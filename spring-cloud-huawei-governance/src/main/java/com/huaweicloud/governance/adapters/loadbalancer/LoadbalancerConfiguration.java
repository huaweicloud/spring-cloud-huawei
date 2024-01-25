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

package com.huaweicloud.governance.adapters.loadbalancer;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestTransformer;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
@ConditionalOnProperty(value = "spring.cloud.servicecomb.loadbalancer.enabled",
    havingValue = "true", matchIfMissing = true)
@LoadBalancerClients(defaultConfiguration = DecorateLoadBalancerClientConfiguration.class)
public class LoadbalancerConfiguration {
  private List<LoadBalancerRequestTransformer> transformers = Collections.emptyList();

  @Autowired(required = false)
  public void setTransformers(List<LoadBalancerRequestTransformer> transformers) {
    this.transformers = transformers;
  }

  @Bean
  @ConditionalOnMissingBean
  public LoadBalancerRequestFactory loadBalancerRequestFactory(LoadBalancerClient loadBalancerClient) {
    return new DecorateLoadBalancerRequestFactory(loadBalancerClient, this.transformers);
  }

  @Bean
  public InstanceIsolationServiceInstanceFilter instanceIsolationServiceInstanceFilter(Environment environment,
      FallbackDiscoveryProperties fallbackDiscoveryProperties) {
    return new InstanceIsolationServiceInstanceFilter(environment, fallbackDiscoveryProperties);
  }

  @Bean
  public FallbackDiscoveryProperties fallbackDiscoveryProperties() {
    return new FallbackDiscoveryProperties();
  }
}
