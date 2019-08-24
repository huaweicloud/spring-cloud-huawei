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

package org.springframework.cloud.servicecomb.discovery.discovery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import org.springframework.cloud.servicecomb.discovery.ConditionalOnServiceCombDiscoveryEnabled;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

@Configuration
@ConditionalOnServiceCombDiscoveryEnabled
@AutoConfigureBefore({SimpleDiscoveryClientAutoConfiguration.class,
    CommonsClientAutoConfiguration.class})
public class ServiceCombDiscoveryClientConfiguration {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombDiscoveryClientConfiguration.class);

  @Bean
  @ConditionalOnMissingBean
  public ServiceCombDiscoveryProperties serviceCombProperties() {
    return new ServiceCombDiscoveryProperties();
  }

  @Bean
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.discovery.enabled", matchIfMissing = true)
  public ServiceCombClient serviceCombClient(ServiceCombDiscoveryProperties serviceCombProperties) {
    ServiceCombClientBuilder builder = new ServiceCombClientBuilder();
    builder.setUrl(serviceCombProperties.getAddress()).setAutoDiscovery(serviceCombProperties.isAutoDiscovery());
    return builder.createServiceCombClient();
  }

  @Bean
  public DiscoveryClient serviceCombDiscoveryClient(
      ServiceCombDiscoveryProperties discoveryProperties, ServiceCombClient serviceCombClient) {
    return new ServiceCombDiscoveryClient(discoveryProperties, serviceCombClient);
  }
}
