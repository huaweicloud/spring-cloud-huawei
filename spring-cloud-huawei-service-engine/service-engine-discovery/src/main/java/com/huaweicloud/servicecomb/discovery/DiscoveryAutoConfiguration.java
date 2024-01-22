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

package com.huaweicloud.servicecomb.discovery;

import java.util.List;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.DiscoveryBootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombSSLProperties;
import com.huaweicloud.service.engine.common.disovery.ServiceCenterUtils;
import com.huaweicloud.servicecomb.discovery.authentication.ServiceCombAuthenticationAdapter;
import com.huaweicloud.servicecomb.discovery.discovery.DiscoveryProperties;
import com.huaweicloud.servicecomb.discovery.loadbalancer.ServiceCombAffinityTagFilterAdapter;
import com.huaweicloud.servicecomb.discovery.loadbalancer.ServiceCombCanaryFilterAdapter;
import com.huaweicloud.servicecomb.discovery.loadbalancer.ServiceCombZoneAwareFilterAdapter;

@Configuration
@ConditionalOnServiceCombDiscoveryEnabled
@EnableConfigurationProperties({DiscoveryProperties.class})
public class DiscoveryAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public ServiceCenterClient serviceCenterClient(DiscoveryBootstrapProperties discoveryProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    return ServiceCenterUtils.serviceCenterClient(discoveryProperties, serviceCombSSLProperties, authHeaderProviders);
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceCenterWatch serviceCenterWatch(DiscoveryBootstrapProperties discoveryProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    return ServiceCenterUtils.serviceCenterWatch(discoveryProperties, serviceCombSSLProperties, authHeaderProviders);
  }

  @Bean
  @ConditionalOnExpression("${" + GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED + ":true}"
      + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED + ":true}")
  public ServiceCombAuthenticationAdapter serviceCombAuthenticationAdapter(ServiceCenterClient serviceCenterClient) {
    return new ServiceCombAuthenticationAdapter(serviceCenterClient);
  }

  @Bean
  public ServiceCombCanaryFilterAdapter serviceCombCanaryFilterAdapter() {
    return new ServiceCombCanaryFilterAdapter();
  }

  @Bean
  public ServiceCombZoneAwareFilterAdapter serviceCombZoneAwareFilterAdapter() {
    return new ServiceCombZoneAwareFilterAdapter();
  }

  @Bean
  public ServiceCombAffinityTagFilterAdapter serviceCombAffinityTagFilterAdapter() {
    return new ServiceCombAffinityTagFilterAdapter();
  }
}
