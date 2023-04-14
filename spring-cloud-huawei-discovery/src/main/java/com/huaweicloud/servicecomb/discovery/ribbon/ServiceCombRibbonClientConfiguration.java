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

package com.huaweicloud.servicecomb.discovery.ribbon;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;

@Configuration
public class ServiceCombRibbonClientConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public ILoadBalancer ribbonLoadBalancer(
      IRule rule, AbstractServerList<Server> serverList) {
    return new ServiceCombLoadBalancer(rule, serverList);
  }

  @Bean
  @ConditionalOnMissingBean
  public AbstractServerList<Server> ribbonServerList(IClientConfig config, DiscoveryClient discoveryClient,
      ServiceCombRegistration serviceCombRegistration) {
    ServiceCombServerList serverList = new ServiceCombServerList(discoveryClient, serviceCombRegistration);
    serverList.initWithNiwsConfig(config);
    return serverList;
  }

  @Bean
  @ConditionalOnMissingBean
  public IRule ribbonRule() {
    return new ServiceCombRoundRobinRule();
  }
}
