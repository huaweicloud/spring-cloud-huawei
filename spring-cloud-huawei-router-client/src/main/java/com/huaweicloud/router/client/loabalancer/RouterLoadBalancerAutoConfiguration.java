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

package com.huaweicloud.router.client.loabalancer;

import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestTransformer;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClients;
import org.springframework.context.annotation.Bean;

/**
 * This class is adapted from LoadBalancerAutoConfiguration. When upgrading spring cloud, this may need change.
 *
 * Intentions:
 *
 * 1. set up LoadBalancerClients defaultConfiguration
 * 2. custom LoadBalancerRequestFactory to get HttpRequest object
 */
@LoadBalancerClients(defaultConfiguration = RouterLoadBalancerClientConfiguration.class)
public class RouterLoadBalancerAutoConfiguration {
  @Autowired(required = false)
  private List<LoadBalancerRequestTransformer> transformers = Collections.emptyList();

  @Bean
  @ConditionalOnMissingBean
  public LoadBalancerRequestFactory loadBalancerRequestFactory(LoadBalancerClient loadBalancerClient) {
    return new RouterLoadBalancerRequestFactory(loadBalancerClient, this.transformers);
  }
}
