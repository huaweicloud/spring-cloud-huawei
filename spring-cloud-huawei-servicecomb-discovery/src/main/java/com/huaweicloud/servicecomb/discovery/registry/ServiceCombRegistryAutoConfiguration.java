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

package com.huaweicloud.servicecomb.discovery.registry;


import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.servicecomb.discovery.ConditionalOnServiceCombEnabled;
import com.huaweicloud.servicecomb.discovery.DiscoveryAutoConfiguration;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

@Configuration
@ConditionalOnProperty(value = "spring.cloud.servicecomb.discovery.enabled", matchIfMissing = true)
@ConditionalOnServiceCombEnabled
@EnableConfigurationProperties
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)//enable custom auto
@AutoConfigureAfter({AutoServiceRegistrationConfiguration.class,
    AutoServiceRegistrationAutoConfiguration.class, DiscoveryAutoConfiguration.class})
public class ServiceCombRegistryAutoConfiguration {
  @Bean
  public ServiceCombServiceRegistry serviceCombServiceRegistry(
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      ServiceCenterClient serviceCenterClient,
      ServiceCenterWatch serviceCenterWatch) {
    return new ServiceCombServiceRegistry(
        serviceCombDiscoveryProperties, serviceCenterClient, serviceCenterWatch);
  }

  @Bean
  @ConditionalOnBean(AutoServiceRegistrationProperties.class)
  public ServiceCombRegistration serviceCombRegistration(
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties, TagsProperties tagsProperties) {
    return new ServiceCombRegistration(serviceCombDiscoveryProperties, tagsProperties);
  }

  @Bean
  @ConditionalOnBean(AutoServiceRegistrationProperties.class)
  public ServiceCombAutoServiceRegistration serviceCombAutoServiceRegistration(
      ServiceCombServiceRegistry registry,
      AutoServiceRegistrationProperties autoServiceRegistrationProperties,
      ServiceCombRegistration registration) {
    return new ServiceCombAutoServiceRegistration(registry,
        autoServiceRegistrationProperties, registration);
  }
}

