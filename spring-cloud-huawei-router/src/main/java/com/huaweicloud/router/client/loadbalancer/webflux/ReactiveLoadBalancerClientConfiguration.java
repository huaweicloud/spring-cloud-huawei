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

package com.huaweicloud.router.client.loadbalancer.webflux;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.gateway.config.GatewayLoadBalancerProperties;
import org.springframework.cloud.gateway.config.GatewayReactiveLoadBalancerClientAutoConfiguration;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledGlobalFilter;
import org.springframework.cloud.loadbalancer.config.LoadBalancerAutoConfiguration;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass(name = {"org.springframework.cloud.gateway.filter.GlobalFilter"})
@AutoConfigureBefore(GatewayReactiveLoadBalancerClientAutoConfiguration.class)
@AutoConfigureAfter(LoadBalancerAutoConfiguration.class)
public class ReactiveLoadBalancerClientConfiguration {
  @Bean
  @ConditionalOnBean(LoadBalancerClientFactory.class)
  @ConditionalOnMissingBean(ReactiveLoadBalancerClientFilterExtend.class)
  @ConditionalOnEnabledGlobalFilter
  public ReactiveLoadBalancerClientFilterExtend reactiveLoadBalancerClientFilterExtend(
      LoadBalancerClientFactory clientFactory, GatewayLoadBalancerProperties properties) {
    return new ReactiveLoadBalancerClientFilterExtend(clientFactory, properties);
  }
}
