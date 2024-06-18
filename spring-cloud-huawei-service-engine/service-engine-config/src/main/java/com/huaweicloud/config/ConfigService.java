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

package com.huaweicloud.config;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.config.RequestConfig;
import org.apache.servicecomb.config.center.client.ConfigCenterAddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.center.client.model.ConfigCenterConfiguration;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.config.common.ConfigConverter;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.config.kie.client.model.KieConfiguration;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.service.engine.common.configration.bootstrap.BootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ConfigBootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombAkSkProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombSSLProperties;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.service.engine.common.transport.TransportUtils;
import com.huaweicloud.common.util.URLUtil;

public class ConfigService {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigService.class);

  private boolean initialized = false;

  private ConfigConverter configConverter;

  private static final ConfigService INSTANCE = new ConfigService();

  private ConfigService() {

  }

  public static ConfigService getInstance() {
    return INSTANCE;
  }

  public ConfigConverter getConfigConverter() {
    return this.configConverter;
  }

  public void init(BootstrapProperties bootstrapProperties,
      List<AuthHeaderProvider> authHeaderProviders) {

    if (StringUtils.isEmpty(bootstrapProperties.getConfigBootstrapProperties().getServerAddr())) {
      throw new IllegalArgumentException(
          "Config server address is not configured. "
              + "Please configure config server address or set spring.cloud.servicecomb.config.enabled to false");
    }

    if (initialized) {
      return;
    }

    initialized = true;

    initConfigConverter(bootstrapProperties.getConfigBootstrapProperties());

    if ("kie".equalsIgnoreCase(bootstrapProperties.getConfigBootstrapProperties().getServerType())) {
      initKieConfig(bootstrapProperties,
          authHeaderProviders);
    } else {
      initServiceCenterConfig(bootstrapProperties,
          authHeaderProviders);
    }
  }

  private void initConfigConverter(ConfigBootstrapProperties configProperties) {
    if (StringUtils.isEmpty(configProperties.getFileSource())) {
      configConverter = new ConfigConverter(null);
      return;
    }
    configConverter = new ConfigConverter(Arrays.asList(configProperties.getFileSource().split(",")));
  }

  private ConfigCenterAddressManager configCenterAddressManager(ConfigBootstrapProperties configProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties) {

    List<String> addresses = URLUtil.dealMultiUrl(configProperties.getServerAddr());

    LOGGER.info("initialize config server type={}, address={}.", configProperties.getServerType(), addresses);
    return new ConfigCenterAddressManager(serviceCombAkSkProperties.getProject(), addresses, EventManager.getEventBus());
  }

  private HttpTransport createHttpTransport(boolean sslEnabled,
      ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders, RequestConfig requestConfig) {
    return HttpTransportFactory
        .createHttpTransport(
            TransportUtils.createSSLProperties(sslEnabled, serviceCombSSLProperties),
            getRequestAuthHeaderProvider(authHeaderProviders), requestConfig);
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders()));
      return headers;
    };
  }

  private QueryConfigurationsRequest createQueryConfigurationsRequest(BootstrapProperties bootstrapProperties) {
    QueryConfigurationsRequest request = new QueryConfigurationsRequest();
    request.setApplication(bootstrapProperties.getMicroserviceProperties().getApplication());
    request.setServiceName(bootstrapProperties.getMicroserviceProperties().getName());
    request.setVersion(bootstrapProperties.getMicroserviceProperties().getVersion());
    request.setEnvironment(bootstrapProperties.getMicroserviceProperties().getEnvironment());
    // 需要设置为 null， 并且 query 参数为 revision=null 才会返回 revision 信息。 revision = 是不行的。
    request.setRevision(null);
    return request;
  }

  private void initServiceCenterConfig(BootstrapProperties bootstrapProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    QueryConfigurationsRequest queryConfigurationsRequest;

    ConfigCenterAddressManager addressManager = configCenterAddressManager(bootstrapProperties.getConfigBootstrapProperties(),
        bootstrapProperties.getServiceCombAkSkProperties());
    HttpTransport httpTransport = createHttpTransport(addressManager.sslEnabled(),
        bootstrapProperties.getServiceCombSSLProperties(),
        authHeaderProviders, HttpTransportFactory.defaultRequestConfig().build());
    ConfigCenterClient configCenterClient = new ConfigCenterClient(addressManager, httpTransport);

    queryConfigurationsRequest = createQueryConfigurationsRequest(bootstrapProperties);
    firstPull(bootstrapProperties.getConfigBootstrapProperties(), configCenterClient, queryConfigurationsRequest,
            addressManager);
    ConfigCenterConfiguration configCenterConfiguration = createConfigCenterConfiguration(
        bootstrapProperties.getConfigBootstrapProperties());
    ConfigCenterManager configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus(),
        configConverter, configCenterConfiguration, addressManager);
    configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
    configCenterManager.startConfigCenterManager();
  }

  public void firstPull(ConfigBootstrapProperties configProperties, ConfigCenterClient configCenterClient,
    QueryConfigurationsRequest queryConfigurationsRequest, ConfigCenterAddressManager addressManager) {
    try {
      firstQueryConfigurations(configCenterClient, queryConfigurationsRequest, addressManager);
    } catch (Exception e) {
      if (configProperties.isFirstPullRequired()) {
        throw e;
      } else {
        LOGGER.warn("first pull failed!");
      }
    }
  }

  private void firstQueryConfigurations(ConfigCenterClient configCenterClient, QueryConfigurationsRequest queryConfigurationsRequest,
          ConfigCenterAddressManager addressManager) {
    int index = 0;
    while (index < 3) {
      String address = addressManager.address();
      try {
        QueryConfigurationsResponse response = configCenterClient.queryConfigurations(queryConfigurationsRequest,
                address);
        if (response.isChanged()) {
          configConverter.updateData(response.getConfigurations());
        }
        queryConfigurationsRequest.setRevision(response.getRevision());
        break;
      } catch (Exception e) {
        if (index == 2) {
          throw e;
        }
        LOGGER.warn("config-center firstQueryConfigurations failed, config address {} and ignore {}", address,
                e.getMessage());
      }
      index++;
    }
  }

  private ConfigCenterConfiguration createConfigCenterConfiguration(ConfigBootstrapProperties configProperties) {
    ConfigCenterConfiguration configCenterConfiguration = new ConfigCenterConfiguration();
    configCenterConfiguration.setRefreshIntervalInMillis(configProperties.getConfigCenter().getRefreshInterval());
    return configCenterConfiguration;
  }

  private KieAddressManager createKieAddressManager(List<String> addresses) {
    return new KieAddressManager(addresses, EventManager.getEventBus());
  }

  private KieAddressManager configKieAddressManager(ConfigBootstrapProperties configProperties) {
    List<String> addresses = URLUtil.dealMultiUrl(configProperties.getServerAddr());
    LOGGER.info("initialize config server type={}, address={}.", configProperties.getServerType(), addresses);
    return createKieAddressManager(addresses);
  }

  private KieConfiguration createKieConfiguration(BootstrapProperties bootstrapProperties) {
    return new KieConfiguration().setAppName(bootstrapProperties.getMicroserviceProperties().getApplication())
        .setFirstPullRequired(bootstrapProperties.getConfigBootstrapProperties().isFirstPullRequired())
        .setCustomLabel(bootstrapProperties.getConfigBootstrapProperties().getKie().getCustomLabel())
        .setCustomLabelValue(bootstrapProperties.getConfigBootstrapProperties().getKie().getCustomLabelValue())
        .setEnableAppConfig(bootstrapProperties.getConfigBootstrapProperties().getKie().isEnableAppConfig())
        .setEnableCustomConfig(bootstrapProperties.getConfigBootstrapProperties().getKie().isEnableCustomConfig())
        .setEnableLongPolling(bootstrapProperties.getConfigBootstrapProperties().getKie().isEnableLongPolling())
        .setEnableServiceConfig(bootstrapProperties.getConfigBootstrapProperties().getKie().isEnableServiceConfig())
        .setEnableVersionConfig(bootstrapProperties.getConfigBootstrapProperties().getKie().isEnableVersionConfig())
        .setEnvironment(bootstrapProperties.getMicroserviceProperties().getEnvironment())
        .setPollingWaitInSeconds(
            bootstrapProperties.getConfigBootstrapProperties().getKie().getPollingWaitTimeInSeconds())
        .setRefreshIntervalInMillis(
            bootstrapProperties.getConfigBootstrapProperties().getKie().getRefreshIntervalInMillis())
        .setProject(bootstrapProperties.getServiceCombAkSkProperties().getProject())
        .setServiceName(bootstrapProperties.getMicroserviceProperties().getName())
        .setVersion(bootstrapProperties.getMicroserviceProperties().getVersion());
  }

  private void initKieConfig(BootstrapProperties bootstrapProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    KieAddressManager kieAddressManager = configKieAddressManager(bootstrapProperties.getConfigBootstrapProperties());

    RequestConfig.Builder requestBuilder = HttpTransportFactory.defaultRequestConfig();
    if (bootstrapProperties.getConfigBootstrapProperties().getKie().isEnableLongPolling()
        && bootstrapProperties.getConfigBootstrapProperties().getKie().getPollingWaitTimeInSeconds() >= 0) {
      requestBuilder.setConnectionRequestTimeout(
          bootstrapProperties.getConfigBootstrapProperties().getKie().getPollingWaitTimeInSeconds() * 2 * 1000);
      requestBuilder.setSocketTimeout(
          bootstrapProperties.getConfigBootstrapProperties().getKie().getPollingWaitTimeInSeconds() * 2 * 1000);
    }
    HttpTransport httpTransport = createHttpTransport(kieAddressManager.sslEnabled(),
        bootstrapProperties.getServiceCombSSLProperties(),
        authHeaderProviders, requestBuilder.build());
    KieConfiguration kieConfiguration = createKieConfiguration(bootstrapProperties);
    KieClient kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);
    KieConfigManager kieConfigManager = new KieConfigManager(kieClient, EventManager.getEventBus(), kieConfiguration,
        configConverter, kieAddressManager);
    kieConfigManager.firstPull();
    kieConfigManager.startConfigKieManager();
  }
}
