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
package com.huaweicloud.nacos.config.config;

import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import com.alibaba.cloud.nacos.NacosConfigManager;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.client.NacosPropertySource;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.nacos.api.config.ConfigService;
import com.huaweicloud.nacos.config.NacosConst;

@Order(0)
public class SystemManagedPropertySourceLocator implements PropertySourceLocator {
  private static final Logger LOGGER = LoggerFactory.getLogger(SystemManagedPropertySourceLocator.class);

  private final ConfigService configService;

  private long timeout;

  private String dataId;

  private String group;

  private boolean isRefresh;

  public SystemManagedPropertySourceLocator(NacosConfigManager nacosConfigManager) {
    this.configService = nacosConfigManager.getConfigService();
  }

  @Override
  public PropertySource<?> locate(Environment environment) {
    dataId = environment.getProperty(NacosConst.SERVICECOMB_NACOS_CONFIG_DATA_ID, String.class,
        buildDefaultDataId(environment));
    group = environment.getProperty(NacosConst.SERVICECOMB_NACOS_CONFIG_GROUP, String.class,
        "cse-app-security-group");
    // isRefresh is only true can achieve dynamic updates
    isRefresh = environment.getProperty(NacosConst.SERVICECOMB_NACOS_CONFIG_REFRESH, boolean.class,
        true);
    timeout = environment.getProperty(NacosConst.SERVICECOMB_NACOS_CONFIG_TIMEOUT, Long.class, 3000L);
    CompositePropertySource composite = new CompositePropertySource(NacosConst.SERVICECOMB_NACOS_PROPERTY_SOURCE_NAME);
    loadServicecombNacosProperties(composite);
    return composite;
  }

  private String buildDefaultDataId(Environment environment) {
    String serviceName = environment.getProperty("spring.application.name", String.class, "");
    return NacosConst.SECURITY_CONFIG_DATA_ID_PREFIX + serviceName + NacosConst.SECURITY_CONFIG_DATA_ID_SUFFIX;
  }

  private void loadServicecombNacosProperties(CompositePropertySource composite) {
    if (StringUtils.isEmpty(dataId) || StringUtils.isEmpty(group)) {
      return;
    }
    String fileExtension = NacosDataParserHandler.getInstance().getFileExtension(dataId);
    NacosPropertySource propertySource = new NacosPropertySource(queryNacosPropertyData(fileExtension),
        group, dataId, new Date(), isRefresh);
    // add nacos listener
    NacosPropertySourceRepository.collectNacosPropertySource(propertySource);
    addFirstPropertySource(composite, propertySource);
  }

  private List<PropertySource<?>> queryNacosPropertyData(String fileExtension) {
    try {
      String data = configService.getConfig(dataId, group, timeout);
      if (StringUtils.isEmpty(data)) {
        LOGGER.warn("empty nacos configuration, dataId[{}], group[{}]", dataId, group);
        return Collections.emptyList();
      }
      return NacosDataParserHandler.getInstance().parseNacosData(dataId, data, fileExtension);
    } catch (Exception e) {
      LOGGER.error("get data from Nacos failed, dataId {} ", dataId, e);
    }
    return Collections.emptyList();
  }

  private void addFirstPropertySource(final CompositePropertySource composite,
      NacosPropertySource nacosPropertySource) {
    if (null == nacosPropertySource || nacosPropertySource.getSource().isEmpty()) {
      return;
    }
    composite.addFirstPropertySource(nacosPropertySource);
  }
}
