/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.nacos.config.refresh;

import java.time.Duration;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.AbstractSharedListener;
import com.alibaba.nacos.api.config.listener.Listener;
import com.alibaba.nacos.api.exception.NacosException;
import com.huaweicloud.nacos.config.NacosConfigConst;
import com.huaweicloud.nacos.config.NacosConfigProperties;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceManager;
import com.huaweicloud.nacos.config.NacosPropertySourceRepository;
import com.huaweicloud.nacos.config.locator.NacosPropertySource;

public class NacosConfigContextRefresher implements ApplicationListener<ApplicationReadyEvent>,
    ApplicationContextAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosConfigContextRefresher.class);

  private static final AtomicLong REFRESH_COUNT = new AtomicLong(0);

  private final AtomicBoolean begin = new AtomicBoolean(false);

  private final Map<String, Listener> listenerMap = new ConcurrentHashMap<>();

  private ApplicationContext applicationContext;

  private final NacosConfigProperties properties;

  private final List<NacosConfigServiceManager> configServiceManagers;

  private final NacosConfigRefreshCache refreshCache;

  private final ThreadPoolTaskScheduler taskScheduler;

  private final Environment env;

  private NacosConfigServiceManager currentConfigServiceManager;

  private int failCount = 0;

  public NacosConfigContextRefresher(NacosConfigProperties properties, Environment env,
      List<NacosConfigServiceManager> configServiceManagers) {
    this.properties = properties;
    this.configServiceManagers = configServiceManagers.stream()
        .sorted(Comparator.comparingInt(Ordered::getOrder)).collect(Collectors.toList());
    this.refreshCache = new NacosConfigRefreshCache();
    this.taskScheduler = buildTaskScheduler();
    this.env = env;
  }

  public static long getRefreshCount() {
    return REFRESH_COUNT.get();
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }

  @Override
  public void onApplicationEvent(ApplicationReadyEvent event) {
    if (begin.compareAndSet(false, true) && properties.isRefreshEnabled()) {
      registerNacosListeners();
      if (configServiceManagers.size() > 1) {
        startSchedulerTask();
      }
    }
  }

  private void startSchedulerTask() {
    taskScheduler.scheduleWithFixedDelay(
        this::masterStandbyServerCheck, Duration.ofMillis(properties.getMasterStandbyServerTaskDelay()));
  }

  private void masterStandbyServerCheck() {
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
        && !currentConfigServiceManager.getServerAddr().equals(configServiceManagers.get(0).getServerAddr())
        && configServiceManagers.get(0).checkServerConnect()) {
      reRegisterNacosListeners();
    }
  }

  private void reRegisterNacosListeners() {
    synchronized (NacosConfigContextRefresher.class) {
      shutDownConfigService(currentConfigServiceManager);
      clearConfigListener();
      registerNacosListeners();
    }
  }

  private void clearConfigListener() {
    ConfigService configService = currentConfigServiceManager.getConfigService();
    if (listenerMap.isEmpty()) {
      return;
    }
    for (Map.Entry<String, Listener> entry: listenerMap.entrySet()) {
      configService.removeListener(entry.getKey().split(NacosConfigConst.COMMAS)[0],
          entry.getKey().split(NacosConfigConst.COMMAS)[1], entry.getValue());
    }
    currentConfigServiceManager.resetConfigService();
    listenerMap.clear();
  }

  public static void shutDownConfigService(NacosConfigServiceManager configServiceManager) {
    try {
      configServiceManager.getConfigService().shutDown();
      LOGGER.warn("nacos config server shutDown success, serverAddr=[{}]", configServiceManager.getServerAddr());
    } catch (NacosException e) {
      LOGGER.warn("nacos config server shutDown error, serverAddr=[{}]", configServiceManager.getServerAddr());
    }
  }

  private void registerNacosListeners() {
    int idx = 0;
    while (idx < configServiceManagers.size()) {
      NacosConfigServiceManager configServiceManager = configServiceManagers.get(idx);
      if (!configServiceManager.checkServerConnect()) {
        idx++;
        continue;
      }
      this.currentConfigServiceManager = configServiceManager;
      try {
        for (NacosPropertySource propertySource: NacosPropertySourceRepository.getAll()) {
          if (propertySource.isRefreshable()) {
            registerNacosListener(propertySource.getGroup(), propertySource.getDataId(), configServiceManager);
          }
        }
        return;
      } catch (NacosException e) {
        LOGGER.error("add nacos config listener error, serverAddr=[{}]", configServiceManager.getServerAddr(), e);
      }
      idx++;
    }
  }

  private void registerNacosListener(String group, String dataId, NacosConfigServiceManager configServiceManager)
      throws NacosException {
    String listenerKey = NacosPropertySourceRepository.getMapKey(dataId, group);
    Listener configListener = listenerMap.computeIfAbsent(listenerKey, listener -> new AbstractSharedListener() {
      @Override
      public void innerReceive(String dataId, String group, String configInfo) {
        REFRESH_COUNT.incrementAndGet();
        refreshCache.addRefreshRecord(dataId, group, configInfo);
        applicationContext.publishEvent(new RefreshEvent(this, null, "refresh nacos config"));
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("refresh nacos config, dataId=[{}], group=[{}], data=[{}]", dataId, group, configInfo);
        }
      }
    });
    addNacosConfigListener(dataId, group, configListener, configServiceManager);
  }

  private void addNacosConfigListener(String dataId, String group, Listener listener,
      NacosConfigServiceManager configServiceManager) throws NacosException {
    try {
      configServiceManager.getConfigService().addListener(dataId, group, listener);
      LOGGER.info("add nacos config listener success, dataId=[{}], group=[{}]", dataId, group);
    } catch (NacosException e) {
      LOGGER.error("add nacos config listener error, serverAddr=[{}], dataId=[{}], group=[{}]",
          configServiceManager.getServerAddr(), dataId, group, e);
    }
  }

  private ThreadPoolTaskScheduler buildTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setBeanName("Nacos-Server-Status-Check-Scheduler");
    taskScheduler.initialize();
    return taskScheduler;
  }
}
