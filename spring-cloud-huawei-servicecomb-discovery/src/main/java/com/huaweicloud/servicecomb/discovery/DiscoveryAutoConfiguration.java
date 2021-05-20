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

package com.huaweicloud.servicecomb.discovery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.service.center.client.AddressManager;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import com.huaweicloud.common.transport.TransportUtils;
import com.huaweicloud.common.util.URLUtil;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

@Configuration
@ConditionalOnServiceCombDiscoveryEnabled
public class DiscoveryAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public ServiceCenterClient serviceCenterClient(ServiceCombDiscoveryProperties discoveryProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    AddressManager addressManager = createAddressManager(discoveryProperties);
    SSLProperties sslProperties = TransportUtils
        .createSSLProperties(addressManager.sslEnabled(), serviceCombSSLProperties);
    return new ServiceCenterClient(addressManager, sslProperties,
        getRequestAuthHeaderProvider(authHeaderProviders),
        // TODO: add other headers needed for registration
        "default", new HashMap<>());
  }

  private RequestAuthHeaderProvider getRequestAuthHeaderProvider(List<AuthHeaderProvider> authHeaderProviders) {
    return signRequest -> {
      Map<String, String> headers = new HashMap<>();
      authHeaderProviders.forEach(provider -> headers.putAll(provider.authHeaders()));
      return headers;
    };
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceCenterWatch serviceCenterWatch(ServiceCombDiscoveryProperties discoveryProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    AddressManager addressManager = createAddressManager(discoveryProperties);
    SSLProperties sslProperties = TransportUtils
        .createSSLProperties(addressManager.sslEnabled(), serviceCombSSLProperties);
    return new ServiceCenterWatch(addressManager, sslProperties, getRequestAuthHeaderProvider(authHeaderProviders),
        // TODO: add other headers needed for registration
        "default", new HashMap<>(), EventManager.getEventBus());
  }

  private AddressManager createAddressManager(ServiceCombDiscoveryProperties discoveryProperties) {
    List<String> addresses = URLUtil.getEnvServerURL();
    if (addresses.isEmpty()) {
      addresses = URLUtil.dealMultiUrl(discoveryProperties.getAddress());
    }
    return new AddressManager("default", addresses);
  }
}
