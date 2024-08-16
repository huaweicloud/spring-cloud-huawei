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

package com.alibaba.cloud.nacos.client;

import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.huaweicloud.nacos.config.manager.ConfigServiceManagerUtils;
import com.huaweicloud.nacos.config.manager.NacosConfigManager;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.cloud.nacos.refresh.NacosSnapshotConfigManager;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.core.env.PropertySource;

/**
 * Forked and modify from com.alibaba.cloud.nacos.client.NacosPropertySourceBuilder.java
 *
 * <p>
 *  change to using master standby config service to query config data
 * </p>
 *
 * @author xiaojing
 * @author pbting
 */
public class NacosPropertySourceBuilder {

  private static final Logger log = LoggerFactory
      .getLogger(NacosPropertySourceBuilder.class);

  private final List<NacosConfigManager> configServiceManagers;

  private long timeout;

  public NacosPropertySourceBuilder(List<NacosConfigManager> nacosConfigManagers, long timeout) {
    this.configServiceManagers = nacosConfigManagers.stream()
        .sorted(Comparator.comparingInt(NacosConfigManager::getOrder)).collect(Collectors.toList());
    this.timeout = timeout;
  }

  public long getTimeout() {
    return timeout;
  }

  public void setTimeout(long timeout) {
    this.timeout = timeout;
  }

  public List<NacosConfigManager> getConfigServiceManagers() {
    return configServiceManagers;
  }

  /**
   * @param dataId Nacos dataId
   * @param group Nacos group
   */
  NacosPropertySource build(String dataId, String group, String fileExtension,
      boolean isRefreshable) {
    List<PropertySource<?>> propertySources = loadNacosData(dataId, group,
        fileExtension);
    NacosPropertySource nacosPropertySource = new NacosPropertySource(propertySources,
        group, dataId, new Date(), isRefreshable);
    NacosPropertySourceRepository.collectNacosPropertySource(nacosPropertySource);
    return nacosPropertySource;
  }

  NacosPropertySource buildWithContext(String dataId, String group, String fileExtension, String context) {
    List<PropertySource<?>> propertySources = Collections.emptyList();
    try {
      propertySources = NacosDataParserHandler.getInstance().parseNacosData(dataId, context, fileExtension);
    } catch (Exception e) {
      log.error("build config error, dataId: {}, group: {}, context: {}", dataId, group, context, e);
    }
    NacosPropertySource nacosPropertySource = new NacosPropertySource(propertySources,
        group, dataId, new Date(), true);
    NacosPropertySourceRepository.collectNacosPropertySource(nacosPropertySource);
    return nacosPropertySource;
  }

  private List<PropertySource<?>> loadNacosData(String dataId, String group,
      String fileExtension) {
    String data = null;
    try {
      String configSnapshot = NacosSnapshotConfigManager.getAndRemoveConfigSnapshot(dataId, group);
      if (StringUtils.isEmpty(configSnapshot)) {
        log.debug("get config from nacos, dataId: {}, group: {}", dataId, group);
        data = ConfigServiceManagerUtils.chooseConfigManager(configServiceManagers)
            .getConfigService().getConfig(dataId, group, timeout);
      } else {
        log.debug("get config from memory snapshot, dataId: {}, group: {}",
            dataId, group);
        data = configSnapshot;
      }
      if (StringUtils.isEmpty(data)) {
        log.warn(
            "Ignore the empty nacos configuration and get it based on dataId[{}] & group[{}]",
            dataId, group);
        return Collections.emptyList();
      }
      if (log.isDebugEnabled()) {
        log.debug(String.format(
            "Loading nacos data, dataId: '%s', group: '%s', data: %s", dataId,
            group, data));
      }
      return NacosDataParserHandler.getInstance().parseNacosData(dataId, data,
          fileExtension);
    } catch (Exception e) {
      log.error("parse data from Nacos error,dataId:{},data:{}", dataId, data, e);
    }
    return Collections.emptyList();
  }
}
