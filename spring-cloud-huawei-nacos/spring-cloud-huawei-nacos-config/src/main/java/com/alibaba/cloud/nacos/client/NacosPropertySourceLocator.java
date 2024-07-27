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

import java.util.List;

import com.huaweicloud.nacos.config.NacosConfigConst;
import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.huaweicloud.nacos.config.client.NacosPropertySourceExtendLocator;
import com.huaweicloud.nacos.config.client.PropertyConfigItem;
import com.huaweicloud.nacos.config.manager.NacosConfigManager;
import com.alibaba.cloud.nacos.parser.NacosDataParserHandler;
import com.alibaba.cloud.nacos.refresh.NacosContextRefresher;

import org.apache.commons.lang3.StringUtils;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

/**
 * Forked and modify from com.alibaba.cloud.nacos.client.NacosPropertySourceLocator.java
 *
 * <p>
 *   add security/default-router configs load and change to using master standby config service to query config data
 * </p>
 *
 * @author xiaojing
 * @author pbting
 */
@Order(0)
public class NacosPropertySourceLocator implements PropertySourceLocator {
  private static final String NACOS_PROPERTY_SOURCE_NAME = "NACOS";

  private static final String SEP1 = "-";

  private static final String DOT = ".";

  private NacosPropertySourceBuilder nacosPropertySourceBuilder;

  private NacosConfigProperties nacosConfigProperties;

  private List<NacosConfigManager> nacosConfigManagers;

  /**
   * recommend to use
   * {@link NacosPropertySourceLocator#NacosPropertySourceLocator(List, NacosConfigProperties)}.
   * @param nacosConfigProperties nacosConfigProperties
   */
  @Deprecated
  public NacosPropertySourceLocator(NacosConfigProperties nacosConfigProperties) {
    this.nacosConfigProperties = nacosConfigProperties;
  }

  public NacosPropertySourceLocator(List<NacosConfigManager> nacosConfigManagers, NacosConfigProperties properties) {
    this.nacosConfigManagers = nacosConfigManagers;
    this.nacosConfigProperties = properties;
  }

  @Override
  public PropertySource<?> locate(Environment env) {
    nacosConfigProperties.setEnvironment(env);
    long timeout = nacosConfigProperties.getTimeout();
    nacosPropertySourceBuilder = new NacosPropertySourceBuilder(nacosConfigManagers,
        timeout);
    String name = nacosConfigProperties.getName();

    String dataIdPrefix = nacosConfigProperties.getPrefix();
    if (StringUtils.isEmpty(dataIdPrefix)) {
      dataIdPrefix = name;
    }

    if (StringUtils.isEmpty(dataIdPrefix)) {
      dataIdPrefix = env.getProperty("spring.application.name");
    }

    CompositePropertySource composite = new CompositePropertySource(
        NACOS_PROPERTY_SOURCE_NAME);

    loadSharedConfiguration(composite);
    loadExtConfiguration(composite);
    loadApplicationConfiguration(composite, dataIdPrefix, nacosConfigProperties, env);

    // load security configs
    loadSecurityConfigs(composite, env);

    // load router configs
    loadLabelRouterConfigs(composite, env);
    return composite;
  }

  private void loadLabelRouterConfigs(CompositePropertySource composite, Environment env) {
    if (!env.getProperty(NacosConfigConst.ROUTER_CONFIG_DEFAULT_LOAD_ENABLED, boolean.class, false)) {
      return;
    }
    NacosPropertySourceExtendLocator extendLocator = new NacosPropertySourceExtendLocator(nacosConfigProperties);
    List<PropertyConfigItem> routerProperties = extendLocator.loadRouterProperties();
    if (CollectionUtils.isEmpty(routerProperties)) {
      return;
    }
    for (PropertyConfigItem item: routerProperties) {
      NacosPropertySource propertySource = nacosPropertySourceBuilder.buildWithContext(item.getDataId(),
          item.getGroup(), item.getType(), item.getContent());
      if (propertySource == null) {
        continue;
      }
      this.addFirstPropertySource(composite, propertySource, false);
    }
  }

  private void loadSecurityConfigs(CompositePropertySource composite, Environment env) {
    String group = "cse-app-security-group";
    String dataId = buildIncludeServiceNameDataId(env, NacosConfigConst.SECURITY_CONFIG_DATA_ID_PREFIX);
    loadNacosDataIfPresent(composite, dataId, group, NacosConfigConst.DEFAULT_CONFIG_FILE_EXTENSION, true);
  }

  private String buildIncludeServiceNameDataId(Environment environment, String dataIdPrefix) {
    String serviceName = environment.getProperty("spring.application.name", String.class, "");
    return dataIdPrefix + serviceName + DOT + NacosConfigConst.DEFAULT_CONFIG_FILE_EXTENSION;
  }

  /**
   * load shared configuration.
   */
  private void loadSharedConfiguration(
      CompositePropertySource compositePropertySource) {
    List<NacosConfigProperties.Config> sharedConfigs = nacosConfigProperties
        .getSharedConfigs();
    if (!CollectionUtils.isEmpty(sharedConfigs)) {
      checkConfiguration(sharedConfigs, "shared-configs");
      loadNacosConfiguration(compositePropertySource, sharedConfigs);
    }
  }

  /**
   * load extensional configuration.
   */
  private void loadExtConfiguration(CompositePropertySource compositePropertySource) {
    List<NacosConfigProperties.Config> extConfigs = nacosConfigProperties
        .getExtensionConfigs();
    if (!CollectionUtils.isEmpty(extConfigs)) {
      checkConfiguration(extConfigs, "extension-configs");
      loadNacosConfiguration(compositePropertySource, extConfigs);
    }
  }

  /**
   * load configuration of application.
   */
  private void loadApplicationConfiguration(
      CompositePropertySource compositePropertySource, String dataIdPrefix,
      NacosConfigProperties properties, Environment environment) {
    String fileExtension = properties.getFileExtension();
    String nacosGroup = properties.getGroup();
    // load directly once by default
    loadNacosDataIfPresent(compositePropertySource, dataIdPrefix, nacosGroup,
        fileExtension, true);
    // load with suffix, which have a higher priority than the default
    loadNacosDataIfPresent(compositePropertySource,
        dataIdPrefix + DOT + fileExtension, nacosGroup, fileExtension, true);
    // Loaded with profile, which have a higher priority than the suffix
    for (String profile : environment.getActiveProfiles()) {
      String dataId = dataIdPrefix + SEP1 + profile + DOT + fileExtension;
      loadNacosDataIfPresent(compositePropertySource, dataId, nacosGroup,
          fileExtension, true);
    }
  }

  private void loadNacosConfiguration(final CompositePropertySource composite,
      List<NacosConfigProperties.Config> configs) {
    for (NacosConfigProperties.Config config : configs) {
      loadNacosDataIfPresent(composite, config.getDataId(), config.getGroup(),
          NacosDataParserHandler.getInstance()
              .getFileExtension(config.getDataId()),
          config.isRefresh());
    }
  }

  private void checkConfiguration(List<NacosConfigProperties.Config> configs,
      String tips) {
    for (int i = 0; i < configs.size(); i++) {
      String dataId = configs.get(i).getDataId();
      if (dataId == null || dataId.trim().length() == 0) {
        throw new IllegalStateException(String.format(
            "the [ spring.cloud.nacos.config.%s[%s] ] must give a dataId",
            tips, i));
      }
    }
  }

  private void loadNacosDataIfPresent(final CompositePropertySource composite,
      final String dataId, final String group, String fileExtension,
      boolean isRefreshable) {
    if (null == dataId || dataId.trim().length() < 1) {
      return;
    }
    if (null == group || group.trim().length() < 1) {
      return;
    }
    NacosPropertySource propertySource = this.loadNacosPropertySource(dataId, group,
        fileExtension, isRefreshable);
    this.addFirstPropertySource(composite, propertySource, false);
  }

  private NacosPropertySource loadNacosPropertySource(final String dataId,
      final String group, String fileExtension, boolean isRefreshable) {
    if (NacosContextRefresher.getRefreshCount() != 0) {
      if (!isRefreshable) {
        return NacosPropertySourceRepository.getNacosPropertySource(dataId,
            group);
      }
    }
    return nacosPropertySourceBuilder.build(dataId, group, fileExtension,
        isRefreshable);
  }

  /**
   * Add the nacos configuration to the first place and maybe ignore the empty
   * configuration.
   */
  private void addFirstPropertySource(final CompositePropertySource composite,
      NacosPropertySource nacosPropertySource, boolean ignoreEmpty) {
    if (null == nacosPropertySource || null == composite) {
      return;
    }
    if (ignoreEmpty && nacosPropertySource.getSource().isEmpty()) {
      return;
    }
    composite.addFirstPropertySource(nacosPropertySource);
  }
}
