/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.apache.http.client.config.RequestConfig;
import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
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
import org.apache.commons.lang3.StringUtils;

import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import com.huaweicloud.common.transport.TransportUtils;
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

  public void init(ServiceCombConfigProperties configProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties, ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    if (initialized) {
      return;
    }

    initialized = true;

    initConfigConverter(configProperties);

    if ("kie".equalsIgnoreCase(configProperties.getServerType())) {
      initKieConfig(configProperties, serviceCombAkSkProperties, serviceCombSSLProperties,
          authHeaderProviders);
    } else {
      initServiceCenterConfig(configProperties, serviceCombAkSkProperties, serviceCombSSLProperties,
          authHeaderProviders);
    }
  }

  private void initConfigConverter(ServiceCombConfigProperties configProperties) {
    if (StringUtils.isEmpty(configProperties.getFileSource())) {
      configConverter = new ConfigConverter(null);
    }
    configConverter = new ConfigConverter(Arrays.asList(configProperties.getFileSource().split(",")));
  }

  private AddressManager configCenterAddressManager(ServiceCombConfigProperties configProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties) {
    List<String> addresses = URLUtil.getEnvConfigUrl();
    if (addresses.isEmpty()) {
      addresses = URLUtil.dealMultiUrl(configProperties.getServerAddr());
    }
    LOGGER
        .info("initialize config server type={}, address={}.", configProperties.getServerType(), addresses.toString());
    return new AddressManager(serviceCombAkSkProperties.getProject(), addresses);
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

  private QueryConfigurationsRequest createQueryConfigurationsRequest(ServiceCombConfigProperties configProperties) {
    QueryConfigurationsRequest request = new QueryConfigurationsRequest();
    request.setApplication(configProperties.getAppName());
    request.setServiceName(configProperties.getServiceName());
    request.setVersion(configProperties.getVersion());
    request.setEnvironment(configProperties.getEnv());
    // 需要设置为 null， 并且 query 参数为 revision=null 才会返回 revision 信息。 revision = 是不行的。
    request.setRevision(null);
    return request;
  }

  private void initServiceCenterConfig(ServiceCombConfigProperties configProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties, ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    QueryConfigurationsRequest queryConfigurationsRequest;

    AddressManager addressManager = configCenterAddressManager(configProperties, serviceCombAkSkProperties);
    HttpTransport httpTransport = createHttpTransport(addressManager.sslEnabled(), serviceCombSSLProperties,
        authHeaderProviders, HttpTransportFactory.defaultRequestConfig().build());
    ConfigCenterClient configCenterClient = new ConfigCenterClient(addressManager, httpTransport);

    queryConfigurationsRequest = createQueryConfigurationsRequest(configProperties);
    QueryConfigurationsResponse response = configCenterClient
        .queryConfigurations(queryConfigurationsRequest);
    if (response.isChanged()) {
      configConverter.updateData(response.getConfigurations());
    }
    queryConfigurationsRequest.setRevision(response.getRevision());
    ConfigCenterManager configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus(),
        configConverter);
    configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
    configCenterManager.startConfigCenterManager();
  }

  private KieAddressManager createKieAddressManager(List<String> addresses) {
    return new KieAddressManager(addresses);
  }

  private KieAddressManager configKieAddressManager(ServiceCombConfigProperties configProperties) {
    List<String> addresses = URLUtil.getEnvConfigUrl();
    if (addresses.isEmpty()) {
      addresses = URLUtil.dealMultiUrl(configProperties.getServerAddr());
    }
    LOGGER
        .info("initialize config server type={}, address={}.", configProperties.getServerType(), addresses.toString());
    return createKieAddressManager(addresses);
  }

  private KieConfiguration createKieConfiguration(ServiceCombConfigProperties configProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties) {
    return new KieConfiguration().setAppName(configProperties.getAppName())
        .setFirstPullRequired(configProperties.isFirstPullRequired())
        .setCustomLabel(configProperties.getKie().getCustomLabel())
        .setCustomLabelValue(configProperties.getKie().getCustomLabelValue())
        .setEnableAppConfig(configProperties.getKie().isEnableAppConfig())
        .setEnableCustomConfig(configProperties.getKie().isEnableCustomConfig())
        .setEnableLongPolling(configProperties.getKie().isEnableLongPolling())
        .setEnableServiceConfig(configProperties.getKie().isEnableServiceConfig())
        .setEnvironment(configProperties.getEnv())
        .setPollingWaitInSeconds(configProperties.getKie().getPollingWaitTimeInSeconds())
        .setProject(serviceCombAkSkProperties.getProject())
        .setServiceName(configProperties.getServiceName());
  }

  private void initKieConfig(ServiceCombConfigProperties configProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties, ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    KieAddressManager kieAddressManager = configKieAddressManager(configProperties);

    RequestConfig.Builder requestBuilder = HttpTransportFactory.defaultRequestConfig();
    if (configProperties.getKie().isEnableLongPolling()
        && configProperties.getKie().getPollingWaitTimeInSeconds() >= 0) {
      requestBuilder.setConnectionRequestTimeout(configProperties.getKie().getPollingWaitTimeInSeconds() * 2 * 1000);
      requestBuilder.setSocketTimeout(configProperties.getKie().getPollingWaitTimeInSeconds() * 2 * 1000);
    }
    HttpTransport httpTransport = createHttpTransport(kieAddressManager.sslEnabled(), serviceCombSSLProperties,
        authHeaderProviders, requestBuilder.build());
    KieConfiguration kieConfiguration = createKieConfiguration(configProperties, serviceCombAkSkProperties);
    KieClient kieClient = new KieClient(kieAddressManager, httpTransport, kieConfiguration);
    KieConfigManager kieConfigManager = new KieConfigManager(kieClient, EventManager.getEventBus(), kieConfiguration,
        configConverter);
    kieConfigManager.firstPull();
    kieConfigManager.startConfigKieManager();
  }
}
