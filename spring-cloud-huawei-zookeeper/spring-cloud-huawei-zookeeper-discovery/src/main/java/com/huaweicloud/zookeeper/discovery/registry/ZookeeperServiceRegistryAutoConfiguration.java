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

package com.huaweicloud.zookeeper.discovery.registry;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationConfiguration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.ServiceRegistryAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.huaweicloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import com.huaweicloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import com.huaweicloud.zookeeper.discovery.ZookeeperServiceInstance;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnZookeeperDiscoveryEnabled
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)
@AutoConfigureAfter({AutoServiceRegistrationConfiguration.class, AutoServiceRegistrationAutoConfiguration.class})
public class ZookeeperServiceRegistryAutoConfiguration {
  @Bean
  @ConditionalOnBean(AutoServiceRegistrationProperties.class)
  public ZookeeperRegistration zookeeperRegistration(ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
    return new ZookeeperRegistration(zookeeperDiscoveryProperties);
  }

  @Bean
  public ZookeeperServiceRegistry zookeeperServiceRegistry(Environment environment, ZookeeperRegistration registration,
      ZookeeperDiscoveryProperties discoveryProperties, ServiceDiscovery<ZookeeperServiceInstance> serviceDiscovery) {
    return new ZookeeperServiceRegistry(discoveryProperties, environment, registration, serviceDiscovery);
  }

  @Bean
  @ConditionalOnBean(AutoServiceRegistrationProperties.class)
  public ZookeeperAutoServiceRegistration zookeeperAutoServiceRegistration(ZookeeperServiceRegistry registry,
      AutoServiceRegistrationProperties autoServiceRegistrationProperties, ZookeeperRegistration registration) {
    return new ZookeeperAutoServiceRegistration(registry, autoServiceRegistrationProperties, registration);
  }
}
