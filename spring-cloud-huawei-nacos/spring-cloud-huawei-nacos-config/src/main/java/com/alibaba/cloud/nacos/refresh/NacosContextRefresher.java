/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.refresh;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import com.huaweicloud.nacos.config.NacosConfigConst;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.huaweicloud.nacos.config.manager.ConfigServiceManagerUtils;
import com.huaweicloud.nacos.config.manager.NacosConfigManager;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * Forked and modify from com.alibaba.cloud.nacos.refresh.NacosContextRefresher.java
 *
 * <p>
 *   change to using master standby config service to add listener and add scheduler check master server task
 * </p>
 *
 * On application start up, NacosContextRefresher add nacos listeners to all application
 * level dataIds, when there is a change in the data, listeners will refresh
 * configurations.
 *
 * @author juven.xuxb
 * @author pbting
 * @author freeman
 */
public class NacosContextRefresher
    implements ApplicationListener<ApplicationReadyEvent>, ApplicationContextAware {

  private final static Logger log = LoggerFactory
      .getLogger(NacosContextRefresher.class);

  private static final AtomicLong REFRESH_COUNT = new AtomicLong(0);

  private final boolean isRefreshEnabled;

  private final NacosRefreshHistory nacosRefreshHistory;

  private NacosConfigProperties nacosConfigProperties;

  private List<NacosConfigManager> nacosConfigManagers;

  private ApplicationContext applicationContext;

  private AtomicBoolean ready = new AtomicBoolean(false);

  private Map<String, Listener> listenerMap = new ConcurrentHashMap<>(16);

  private final Environment env;

  private final ThreadPoolTaskScheduler taskScheduler;

  private int failCount = 0;

  private NacosConfigManager currentConfigServiceManager;

  public NacosContextRefresher(List<NacosConfigManager> nacosConfigManagers, NacosRefreshHistory refreshHistory,
      NacosConfigProperties properties, Environment env) {
    this.nacosConfigManagers = nacosConfigManagers.stream()
        .sorted(Comparator.comparingInt(NacosConfigManager::getOrder)).collect(Collectors.toList());
    this.nacosConfigProperties = properties;
    this.nacosRefreshHistory = refreshHistory;
    this.isRefreshEnabled = this.nacosConfigProperties.isRefreshEnabled();
    this.env = env;
    this.taskScheduler = buildTaskScheduler();
  }

  public static long getRefreshCount() {
    return REFRESH_COUNT.get();
  }

  public static void refreshCountIncrement() {
    REFRESH_COUNT.incrementAndGet();
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    // many Spring context
    if (this.ready.compareAndSet(false, true) && isRefreshEnabled()) {
      this.registerNacosListenersForApplications();
      if (nacosConfigProperties.isMasterStandbyEnabled()) {
        startSchedulerTask();
      }
    }
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  private void startSchedulerTask() {
    taskScheduler.scheduleWithFixedDelay(
        this::masterStandbyServerCheck, Duration.ofMillis(nacosConfigProperties.getMasterStandbyServerTaskDelay()));
  }

  private void masterStandbyServerCheck() {
    synchronized (NacosContextRefresher.class) {
      if (!NacosConfigConst.STATUS_UP.equals(currentConfigServiceManager.getConfigService().getServerStatus())) {
        if (failCount == 2) {
          reRegisterNacosListeners();
          failCount = 0;
        } else {
          failCount++;
        }
      } else {
        failCount = 0;
      }
      boolean isRetryMasterServer = env.getProperty(NacosConfigConst.RETRY_MASTER_ENABLED, boolean.class, true);

      // if current server is not master, check whether the master status is healthy, if status UP, use master server.
      if (isRetryMasterServer
          && !currentConfigServiceManager.getServerAddr().equals(nacosConfigManagers.get(0).getServerAddr())
          && nacosConfigManagers.get(0).checkServerConnect()) {
        reRegisterNacosListeners();
      }
    }
  }

  private void reRegisterNacosListeners() {
    shutDownConfigService(currentConfigServiceManager);
    clearConfigListener();
    currentConfigServiceManager.resetConfigService();
    registerNacosListenersForApplications();
  }

  private void clearConfigListener() {
    ConfigService configService = currentConfigServiceManager.getConfigService();
    if (listenerMap.isEmpty()) {
      return;
    }
    for (Map.Entry<String, Listener> entry : listenerMap.entrySet()) {
      String dataId = entry.getKey().split(NacosConfigConst.COMMAS)[0];
      String group = entry.getKey().split(NacosConfigConst.COMMAS)[1];
      configService.removeListener(dataId, group, entry.getValue());
    }
    listenerMap.clear();
  }

  public static void shutDownConfigService(NacosConfigManager configServiceManager) {
    try {
      configServiceManager.getConfigService().shutDown();
      log.warn("nacos config server shutDown success, serverAddr=[{}]", configServiceManager.getServerAddr());
    } catch (NacosException e) {
      log.error("nacos config server shutDown error, serverAddr=[{}]", configServiceManager.getServerAddr());
    }
  }

  /**
   * register Nacos Listeners.
   */
  private void registerNacosListenersForApplications() {
    this.currentConfigServiceManager = ConfigServiceManagerUtils.chooseConfigManager(nacosConfigManagers);
    try {
      for (NacosPropertySource propertySource : NacosPropertySourceRepository.getAll()) {
        if (propertySource.isRefreshable()) {
          registerNacosListener(propertySource.getGroup(), propertySource.getDataId(), currentConfigServiceManager);
        }
      }
    } catch (NacosException e) {
      log.error("add nacos config listener error, serverAddr=[{}]", currentConfigServiceManager.getServerAddr(), e);
    }
  }

  private void registerNacosListener(final String groupKey, final String dataKey, NacosConfigManager configManager)
      throws NacosException {
    String key = NacosPropertySourceRepository.getMapKey(dataKey, groupKey);
    Listener listener = listenerMap.computeIfAbsent(key,
        lst -> new AbstractSharedListener() {
          @Override
          public void innerReceive(String dataId, String group,
              String configInfo) {
            refreshCountIncrement();
            nacosRefreshHistory.addRefreshRecord(dataId, group, configInfo);
            NacosSnapshotConfigManager.putConfigSnapshot(dataId, group,
                configInfo);
            applicationContext.publishEvent(
                new RefreshEvent(this, null, "Refresh Nacos config"));
            if (log.isDebugEnabled()) {
              log.debug(String.format(
                  "Refresh Nacos config group=%s,dataId=%s,configInfo=%s",
                  group, dataId, configInfo));
            }
          }
        });
    addNacosListener(groupKey, dataKey, configManager, listener);
  }

  private void addNacosListener(final String groupKey, final String dataKey, NacosConfigManager configManager,
      Listener listener) throws NacosException {
    try {
      configManager.getConfigService().addListener(dataKey, groupKey, listener);
      log.info("[Nacos Config] Listening config: dataId={}, group={}", dataKey, groupKey);
    } catch (NacosException e) {
      log.warn(String.format(
          "register fail for nacos listener ,dataId=[%s],group=[%s]", dataKey, groupKey), e);
      throw e;
    }
  }

  public NacosConfigProperties getNacosConfigProperties() {
    return nacosConfigProperties;
  }

  public NacosContextRefresher setNacosConfigProperties(
      NacosConfigProperties nacosConfigProperties) {
    this.nacosConfigProperties = nacosConfigProperties;
    return this;
  }

  public boolean isRefreshEnabled() {
    if (null == nacosConfigProperties) {
      return isRefreshEnabled;
    }
    // Compatible with older configurations
    if (nacosConfigProperties.isRefreshEnabled() && !isRefreshEnabled) {
      return false;
    }
    return isRefreshEnabled;
  }

  private ThreadPoolTaskScheduler buildTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setBeanName("Nacos-Server-Status-Check-Scheduler");
    taskScheduler.initialize();
    return taskScheduler;
  }
}
