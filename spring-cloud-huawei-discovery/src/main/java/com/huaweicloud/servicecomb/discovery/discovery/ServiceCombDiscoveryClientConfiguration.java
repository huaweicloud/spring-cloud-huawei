/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.servicecomb.discovery.discovery;

import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import com.huaweicloud.common.configration.bootstrap.BootstrapProperties;
import com.huaweicloud.common.configration.bootstrap.DiscoveryBootstrapProperties;
import com.huaweicloud.servicecomb.discovery.ConditionalOnServiceCombDiscoveryEnabled;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

@Configuration
@ConditionalOnServiceCombDiscoveryEnabled
@AutoConfigureBefore({CommonsClientAutoConfiguration.class})
public class ServiceCombDiscoveryClientConfiguration {
  @Bean
  @Order(100)
  public DiscoveryClient serviceCombDiscoveryClient(
      BootstrapProperties bootstrapProperties, ServiceCenterClient serviceCenterClient,
      ServiceCombRegistration serviceCombRegistration) {
    return new ServiceCombDiscoveryClient(bootstrapProperties, serviceCenterClient, serviceCombRegistration);
  }

  @Bean
  @Order(100)
  public ServiceAddressManager serviceAddressManager(DiscoveryBootstrapProperties discoveryProperties,
      ServiceCenterClient serviceCenterClient, ServiceCombRegistration serviceCombRegistration) {
    return new ServiceAddressManager(discoveryProperties, serviceCenterClient, serviceCombRegistration);
  }
}
