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

package com.huaweicloud.nacos.discovery.registry;

import java.util.List;

import org.springframework.beans.factory.ObjectProvider;
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

import com.huaweicloud.nacos.discovery.ConditionalOnNacosDiscoveryEnabled;
import com.huaweicloud.nacos.discovery.NacosServiceAutoConfiguration;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.manager.NamingServiceManager;

@Configuration(proxyBeanMethods = false)
@EnableConfigurationProperties
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureBefore(ServiceRegistryAutoConfiguration.class)
@AutoConfigureAfter({AutoServiceRegistrationConfiguration.class, AutoServiceRegistrationAutoConfiguration.class,
    NacosServiceAutoConfiguration.class})
public class NacosServiceRegistryAutoConfiguration {

  @Bean
  public NacosServiceRegistry nacosServiceRegistry(List<NamingServiceManager> namingServiceManagers,
      NacosDiscoveryProperties nacosDiscoveryProperties, Environment environment) {
    return new NacosServiceRegistry(namingServiceManagers, nacosDiscoveryProperties, environment);
  }

  @Bean
  @ConditionalOnBean(AutoServiceRegistrationProperties.class)
  public NacosRegistration nacosRegistration(
      ObjectProvider<List<NacosRegistrationMetadataCustomizer>> registrationCustomizers,
      NacosDiscoveryProperties nacosDiscoveryProperties) {
    return new NacosRegistration(registrationCustomizers.getIfAvailable(), nacosDiscoveryProperties);
  }

  @Bean
  @ConditionalOnBean(AutoServiceRegistrationProperties.class)
  public NacosAutoServiceRegistration nacosAutoServiceRegistration(NacosServiceRegistry registry,
      AutoServiceRegistrationProperties autoServiceRegistrationProperties, NacosRegistration registration) {
    return new NacosAutoServiceRegistration(registry, autoServiceRegistrationProperties, registration);
  }
}
