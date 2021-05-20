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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.config.center.client.AddressManager;
import org.apache.servicecomb.config.center.client.ConfigCenterClient;
import org.apache.servicecomb.config.center.client.ConfigCenterManager;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsRequest;
import org.apache.servicecomb.config.center.client.model.QueryConfigurationsResponse;
import org.apache.servicecomb.config.kie.client.KieClient;
import org.apache.servicecomb.config.kie.client.KieConfigManager;
import org.apache.servicecomb.config.kie.client.model.ConfigConstants;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsRequest;
import org.apache.servicecomb.config.kie.client.model.ConfigurationsResponse;
import org.apache.servicecomb.config.kie.client.model.KieAddressManager;
import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpTransport;
import org.apache.servicecomb.http.client.common.HttpTransportFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.util.StringUtils;

import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import com.huaweicloud.common.transport.TransportUtils;
import com.huaweicloud.common.util.URLUtil;

@Configuration
@EnableConfigurationProperties(ServiceCombConfigProperties.class)
@ConditionalOnProperty(name = "spring.cloud.servicecomb.config.enabled", matchIfMissing = true)
public class ServiceCombConfigBootstrapConfiguration {
  private static final String DEFAULT_PROJECT = "default";

  @Bean
  public ConfigWatch configWatch(ContextRefresher contextRefresher) {
    return new ConfigWatch(contextRefresher);
  }

  @Configuration
  @ConditionalOnProperty(name = "spring.cloud.servicecomb.config.serverType", havingValue = "config-center", matchIfMissing = true)
  public static class ConfigCenterConfiguration {
    private final Map<String, Object> configurations = new ConcurrentHashMap<>();

    private QueryConfigurationsRequest queryConfigurationsRequest;

    @Bean
    public AddressManager configCenterAddressManager(ServiceCombConfigProperties configProperties,
        ServiceCombAkSkProperties serviceCombAkSkProperties) {
      List<String> addresses = URLUtil.getEnvConfigUrl();
      if (addresses.isEmpty()) {
        addresses = URLUtil.dealMultiUrl(configProperties.getServerAddr());
      }
      return new AddressManager(serviceCombAkSkProperties.getProject(), addresses);
    }

    @Bean
    public HttpTransport configCenterHttpTransport(AddressManager addressManager,
        ServiceCombSSLProperties serviceCombSSLProperties,
        List<AuthHeaderProvider> authHeaderProviders) {

      return HttpTransportFactory
          .createHttpTransport(
              TransportUtils.createSSLProperties(addressManager.sslEnabled(), serviceCombSSLProperties),
              getRequestAuthHeaderProvider(authHeaderProviders));
    }

    @Bean
    public ConfigCenterClient configCenterClient(HttpTransport configCenterHttpTransport,
        AddressManager configCenterAddressManager) {
      return new ConfigCenterClient(configCenterAddressManager, configCenterHttpTransport);
    }

    @Bean
    public ConfigCenterManager configCenterManager(ConfigCenterClient configCenterClient,
        ServiceCombConfigProperties configProperties) {
      queryConfigurationsRequest = createQueryConfigurationsRequest(configProperties);
      QueryConfigurationsResponse response = configCenterClient
          .queryConfigurations(queryConfigurationsRequest);
      configurations.putAll(response.getConfigurations());
      queryConfigurationsRequest.setRevision(response.getRevision());
      ConfigCenterManager configCenterManager = new ConfigCenterManager(configCenterClient, EventManager.getEventBus(),
          configurations);
      configCenterManager.setQueryConfigurationsRequest(queryConfigurationsRequest);
      configCenterManager.startConfigCenterManager();
      return configCenterManager;
    }

    @Bean
    @DependsOn("configCenterManager")
    public ServiceCombPropertySourceLocator serviceCombPropertySourceLocator(
        ServiceCombConfigProperties serviceCombConfigProperties,
        ServiceCombAkSkProperties serviceCombAkSkProperties) {
      return new ServiceCombPropertySourceLocator(configurations);
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
  }

  @Configuration
  @ConditionalOnProperty(name = "spring.cloud.servicecomb.config.serverType", havingValue = "kie")
  class KieConfiguration {
    private final Map<String, Object> configurations = new ConcurrentHashMap<>();

    private ConfigurationsRequest configurationsRequest;

    @Bean
    public KieAddressManager configKieAddressManager(ServiceCombConfigProperties configProperties,
        ServiceCombAkSkProperties serviceCombAkSkProperties) {
      List<String> addresses = URLUtil.getEnvConfigUrl();
      if (addresses.isEmpty()) {
        addresses = URLUtil.dealMultiUrl(configProperties.getServerAddr());
      }
      return createKieAddressManager(addresses, configProperties, serviceCombAkSkProperties);
    }

    public KieAddressManager createKieAddressManager(List<String> addresses,
        ServiceCombConfigProperties configProperties,
        ServiceCombAkSkProperties serviceCombAkSkProperties) {
      Properties properties = new Properties();
      Map<String, String> configKey = new HashMap<>();
      properties.setProperty(ConfigConstants.KEY_PROJECT,
          StringUtils.isEmpty(serviceCombAkSkProperties.getProject()) ? DEFAULT_PROJECT
              : serviceCombAkSkProperties.getProject());
      properties
          .setProperty(ConfigConstants.KEY_ENABLELONGPOLLING,
              Boolean.toString(configProperties.getEnableLongPolling()));
      properties.setProperty(ConfigConstants.KEY_POLLINGWAITSEC,
          Integer.toString(configProperties.getWatch().getPollingWaitTimeInSeconds()));

      configKey.put(ConfigConstants.KEY_PROJECT, ConfigConstants.KEY_PROJECT);
      configKey.put(ConfigConstants.KEY_ENABLELONGPOLLING, ConfigConstants.KEY_ENABLELONGPOLLING);
      configKey.put(ConfigConstants.KEY_POLLINGWAITSEC, ConfigConstants.KEY_POLLINGWAITSEC);

      return new KieAddressManager(properties, addresses, configKey);
    }

    @Bean
    public HttpTransport configKieHttpTransport(KieAddressManager addressManager,
        ServiceCombSSLProperties serviceCombSSLProperties,
        List<AuthHeaderProvider> authHeaderProviders) {

      return HttpTransportFactory
          .createHttpTransport(
              TransportUtils.createSSLProperties(addressManager.sslEnabled(), serviceCombSSLProperties),
              getRequestAuthHeaderProvider(authHeaderProviders));
    }

    @Bean
    public KieClient kieClient(KieAddressManager kieAddressManager, HttpTransport configKieHttpTransport) {
      return new KieClient(kieAddressManager, configKieHttpTransport);
    }

    @Bean
    public KieConfigManager kieConfigManager(KieClient kieClient, ServiceCombConfigProperties configProperties) {
      ConfigurationsRequest configurationsRequest = createConfigurationsRequest(configProperties);
      ConfigurationsResponse response = kieClient.queryConfigurations(configurationsRequest);
      configurations.putAll(response.getConfigurations());
      configurationsRequest.setRevision(response.getRevision());

      KieConfigManager kieConfigManager = new KieConfigManager(kieClient, EventManager.getEventBus(), configurations);
      kieConfigManager.setConfigurationsRequest(configurationsRequest);
      kieConfigManager.startConfigKieManager();
      return kieConfigManager;
    }

    private ConfigurationsRequest createConfigurationsRequest(ServiceCombConfigProperties configProperties) {
      ConfigurationsRequest request = new ConfigurationsRequest();
      request.setApplication(configProperties.getAppName());
      request.setServiceName(configProperties.getServiceName());
      request.setVersion(configProperties.getVersion());
      request.setEnvironment(configProperties.getEnv());
      // 需要设置为 null， 并且 query 参数为 revision=null 才会返回 revision 信息。 revision = 是不行的。
      request.setRevision(null);
      return request;
    }
  }

  private static RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders()));
      return headers;
    };
  }
}
