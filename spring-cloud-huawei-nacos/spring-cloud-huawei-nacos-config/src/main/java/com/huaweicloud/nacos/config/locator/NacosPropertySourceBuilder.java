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

package com.huaweicloud.nacos.config.locator;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.env.PropertySource;

import com.alibaba.nacos.api.exception.NacosException;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceManager;
import com.huaweicloud.nacos.config.NacosPropertySourceRepository;
import com.huaweicloud.nacos.config.parser.NacosDataParserService;

public class NacosPropertySourceBuilder {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosPropertySourceBuilder.class);

  private final List<NacosConfigServiceManager> configServiceManagers;

  private final long timeout;

  public NacosPropertySourceBuilder(List<NacosConfigServiceManager> configServiceManagers, long timeout) {
    this.configServiceManagers = configServiceManagers.stream().filter(NacosConfigServiceManager::checkServerConnect)
        .sorted(Comparator.comparingInt(Ordered::getOrder)).collect(Collectors.toList());
    this.timeout = timeout;
  }

  public NacosPropertySource buildPropertySource(String dataId, String group, boolean refresh, String fileExtension) {
    List<PropertySource<?>> propertySources = loadNacosConfigData(dataId, group, fileExtension);
    Map<String, Object> source = NacosPropertySource.buildMapSource(propertySources, dataId, group);
    NacosPropertySource propertySource = new NacosPropertySource(group, dataId, refresh, source);
    NacosPropertySourceRepository.setNacosPropertySource(propertySource);
    return propertySource;
  }

  private List<PropertySource<?>> loadNacosConfigData(String dataId, String group, String fileExtension) {
    String data = getConfigDataFromNacos(dataId, group);
    try {
      return NacosDataParserService.getInstance().parseNacosConfigData(dataId, data, fileExtension);
    } catch (IOException e) {
      LOGGER.error("parse nacos config data error, dataId=[{}], group=[{}], data:[{}]", dataId, group, data, e);
    }
    return Collections.emptyList();
  }

  private String getConfigDataFromNacos(String dataId, String group) {
    for (NacosConfigServiceManager configServiceManager: configServiceManagers) {
      try {
        return configServiceManager.getConfigService().getConfig(dataId, group, timeout);
      } catch (NacosException e) {
        LOGGER.error("get config from nacos server error, serverAddr=[{}], dataId=[{}], group=[{}]",
            configServiceManager.getServerAddr(), dataId, group, e);
      }
    }
    return "";
  }
}
