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

import com.huaweicloud.nacos.config.NacosConfigProperties;
import com.huaweicloud.nacos.config.NacosConfigProperties.Config;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceManager;
import com.huaweicloud.nacos.config.NacosConfigConst;
import com.huaweicloud.nacos.config.NacosPropertySourceRepository;
import com.huaweicloud.nacos.config.parser.NacosDataParserService;
import com.huaweicloud.nacos.config.refresh.NacosConfigContextRefresher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

public class NacosPropertySourceLocator implements PropertySourceLocator {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosPropertySourceLocator.class);

  private final NacosConfigProperties properties;

  private final NacosPropertySourceBuilder propertySourceBuilder;

  private static final String PROPERTY_SOURCE_NAME = "NACOS";

  private static final String SEP = "-";

  public NacosPropertySourceLocator(List<NacosConfigServiceManager> configServiceManagers,
      NacosConfigProperties properties) {
    this.properties = properties;
    this.propertySourceBuilder = new NacosPropertySourceBuilder(configServiceManagers, properties.getTimeout());
  }

  @Override
  public PropertySource<?> locate(Environment environment) {
    CompositePropertySource composite = new CompositePropertySource(PROPERTY_SOURCE_NAME);

    // load shared configs
    loadSharedConfigs(composite);

    // load extension configs
    loadExtensionConfigs(composite);

    // load application configs
    loadApplicationConfigs(composite, environment);

    // load security configs
    loadSecurityConfigs(composite, environment);
    return composite;
  }

  private void loadSecurityConfigs(CompositePropertySource composite, Environment env) {
    String group = "cse-app-security-group";
    String dataId = buildSecurityDataId(env);
    loadNacosProperties(composite, dataId, group, true, NacosConfigConst.SECURITY_CONFIG_FILE_EXTENSION);
  }

  private String buildSecurityDataId(Environment environment) {
    String serviceName = environment.getProperty("spring.application.name", String.class, "");
    return NacosConfigConst.SECURITY_CONFIG_DATA_ID_PREFIX + serviceName + NacosConfigConst.DOT
        + NacosConfigConst.SECURITY_CONFIG_FILE_EXTENSION;
  }

  private void loadApplicationConfigs(CompositePropertySource composite, Environment env) {
    String configGroup = properties.getGroup();
    String defaultDataId = properties.getName();
    String fileExtension = properties.getFileExtension();
    boolean refresh = properties.isRefreshEnabled();

    // load serviceName or set name dataId config
    loadNacosProperties(composite, defaultDataId, configGroup, refresh, fileExtension);

    // load serviceName or set name + fileExtension dataId config
    loadNacosProperties(composite, defaultDataId + NacosConfigConst.DOT + fileExtension, configGroup, refresh,
        fileExtension);

    // load serviceName or set name + profile + fileExtension dataId config
    for (String profile : env.getActiveProfiles()) {
      String dataId = defaultDataId + SEP + profile + NacosConfigConst.DOT + fileExtension;
      loadNacosProperties(composite, dataId, configGroup, refresh, fileExtension);
    }
  }

  private void loadExtensionConfigs(CompositePropertySource composite) {
    List<Config> sharedConfigs = properties.getExtensionConfigs();
    List<Config> configs = rebuildConfigParameters(sharedConfigs, "extension-configs");
    if (!CollectionUtils.isEmpty(configs)) {
      loadNacosConfiguration(composite, configs);
    }
  }

  private void loadSharedConfigs(CompositePropertySource composite) {
    List<Config> sharedConfigs = properties.getSharedConfigs();
    List<Config> configs = rebuildConfigParameters(sharedConfigs, "shared-configs");
    if (!CollectionUtils.isEmpty(configs)) {
      loadNacosConfiguration(composite, configs);
    }
  }

  private List<Config> rebuildConfigParameters(List<Config> configs, String tag) {
    List<Config> result = new ArrayList<>();
    if (CollectionUtils.isEmpty(configs)) {
      return result;
    }
    for (int i = 0; i < configs.size(); i++) {
      if (configs.get(i).getDataId() == null || configs.get(i).getDataId().trim().isEmpty()) {
        LOGGER.warn("[{}] configs index=[{}] dataId is null, remove it for load.", tag, i);
        continue;
      }
      result.add(configs.get(i));
    }
    return result;
  }

  private void loadNacosConfiguration(CompositePropertySource composite, List<Config> configs) {
    for (Config config : configs) {
      loadNacosProperties(composite, config.getDataId(), config.getGroup(), config.isRefresh(),
          NacosDataParserService.getInstance().getFileExtension(config.getDataId()));
    }
  }

  private void loadNacosProperties(CompositePropertySource composite, String dataId, String group, boolean refresh,
      String fileExtension) {
    if (group == null || group.trim().isEmpty()) {
        LOGGER.warn("load dataId=[{}] nacos properties group is null, skip for load.", dataId);
        return;
    }
    NacosPropertySource propertySource;
    if (!refresh && NacosConfigContextRefresher.getRefreshCount() != 0) {
        propertySource = NacosPropertySourceRepository.getNacosPropertySource(dataId, group);
    } else {
        propertySource = propertySourceBuilder.buildPropertySource(dataId, group, refresh, fileExtension);
    }
    if (propertySource != null) {
        composite.addFirstPropertySource(propertySource);
    }
  }
}
