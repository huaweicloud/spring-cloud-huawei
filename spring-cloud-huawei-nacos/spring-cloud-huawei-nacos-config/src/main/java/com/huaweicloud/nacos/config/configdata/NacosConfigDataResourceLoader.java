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

package com.huaweicloud.nacos.config.configdata;

import static org.springframework.boot.context.config.ConfigData.Option.IGNORE_IMPORTS;
import static org.springframework.boot.context.config.ConfigData.Option.IGNORE_PROFILES;
import static org.springframework.boot.context.config.ConfigData.Option.PROFILE_SPECIFIC;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.config.ConfigData;
import org.springframework.boot.context.config.ConfigData.Option;
import org.springframework.boot.context.config.ConfigDataLoader;
import org.springframework.boot.context.config.ConfigDataLoaderContext;
import org.springframework.boot.context.config.ConfigDataResourceNotFoundException;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.PropertySource;

import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import com.huaweicloud.nacos.config.NacosConfigProperties;
import com.huaweicloud.nacos.config.NacosPropertySourceRepository;
import com.huaweicloud.nacos.config.locator.NacosPropertySource;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceMasterManager;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceStandbyManager;
import com.huaweicloud.nacos.config.parser.NacosDataParserService;

public class NacosConfigDataResourceLoader implements ConfigDataLoader<NacosConfigDataResource> {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosConfigDataResourceLoader.class);

  private static final String PREFERENCE_LOCAL = "LOCAL";

  private static final String PREFERENCE_REMOTE = "REMOTE";

  @Override
  public ConfigData load(ConfigDataLoaderContext context, NacosConfigDataResource resource) {
    try {
      ConfigService configService = getConfigService(context);
      if (configService == null) {
        LOGGER.warn("has no available server!");
        return null;
      }
      NacosConfigProperties properties = getBean(context, NacosConfigProperties.class);
      long timeout = properties == null ? 3000 : properties.getTimeout();
      List<PropertySource<?>> propertySources = queryPropertiesFromNacos(configService, resource, timeout);
      Map<String, Object> source = NacosPropertySource.buildMapSource(propertySources, resource.getDataId(),
          resource.getGroup());
      NacosPropertySource propertySource = new NacosPropertySource(resource.getGroup(), resource.getDataId(),
          resource.isRefreshEnabled(), source);
      NacosPropertySourceRepository.setNacosPropertySource(propertySource);
      return new ConfigData(propertySources, getOptions(context, resource));
    } catch (Exception e) {
      LOGGER.error("getting import properties from nacos error, source:{}", resource, e);
      if (!resource.isOptionalEnabled()) {
        throw new ConfigDataResourceNotFoundException(resource, e);
      }
    }
    return null;
  }

  private List<PropertySource<?>> queryPropertiesFromNacos(ConfigService configService,
      NacosConfigDataResource resource, long timeout) throws NacosException, IOException {
    String data = configService.getConfig(resource.getDataId(), resource.getGroup(), timeout);
    LOGGER.info("query import config data success, dataId=[{}], group=[{}], data:[{}]", resource.getDataId(),
        resource.getGroup(), data);
    String configName = resource.getGroup() + "@" + resource.getDataId();
    return NacosDataParserService.getInstance().parseNacosConfigData(configName, data, resource.getFileExtension());
  }

  private ConfigService getConfigService(ConfigDataLoaderContext context) {
    NacosConfigServiceMasterManager configServiceMaster = getBean(context, NacosConfigServiceMasterManager.class);
    if (configServiceMaster != null && configServiceMaster.checkServerConnect()) {
      return configServiceMaster.getConfigService();
    }
    NacosConfigServiceStandbyManager configServiceStandby = getBean(context, NacosConfigServiceStandbyManager.class);
    if (configServiceStandby != null && configServiceStandby.checkServerConnect()) {
      return configServiceStandby.getConfigService();
    }
    return null;
  }

  private <T> T getBean(ConfigDataLoaderContext context, Class<T> type) {
    if (context.getBootstrapContext().isRegistered(type)) {
      return context.getBootstrapContext().get(type);
    }
    return null;
  }

  private Option[] getOptions(ConfigDataLoaderContext context,
      NacosConfigDataResource resource) {
    List<Option> options = new ArrayList<>();
    options.add(IGNORE_IMPORTS);
    options.add(IGNORE_PROFILES);
    if (PREFERENCE_REMOTE.equalsIgnoreCase(getPreference(context, resource))) {
      options.add(PROFILE_SPECIFIC);
    }
    return options.toArray(new Option[0]);
  }

  private String getPreference(ConfigDataLoaderContext context, NacosConfigDataResource resource) {
    Binder binder = context.getBootstrapContext().get(Binder.class);
    if (!StringUtils.isEmpty(resource.getPreference())) {
      return resource.getPreference();
    }
    return binder
        .bind("spring.cloud.nacos.config.preference", String.class)
        .orElse(PREFERENCE_LOCAL);
  }
}
