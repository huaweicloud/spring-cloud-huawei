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
package com.huaweicloud.router.client;

import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.loadbalancer.annotation.LoadBalancerClientConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.huaweicloud.router.client.loadbalancer.AffinityTagFilterAdapter;
import com.huaweicloud.router.client.loadbalancer.AffinityTagServiceInstanceFilter;
import com.huaweicloud.router.client.loadbalancer.CanaryFilterAdapter;
import com.huaweicloud.router.client.loadbalancer.SpringCloudRouterDistributor;
import com.huaweicloud.router.client.loadbalancer.ZoneAwareFilterAdapter;
import com.huaweicloud.router.client.loadbalancer.ZoneAwareServiceInstanceFilter;

@Configuration
@ConditionalOnRouterEnabled
@ComponentScan(basePackages = {"org.apache.servicecomb.router"})
@AutoConfigureAfter(LoadBalancerClientConfiguration.class)
public class RouterClientAutoConfiguration {
  @Bean
  public AbstractRouterDistributor<ServiceInstance> springCloudRouterDistributor(
      CanaryFilterAdapter adapter) {
    return new SpringCloudRouterDistributor(adapter);
  }

  @Bean
  @ConditionalOnMissingBean(ZoneAwareServiceInstanceFilter.class)
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.discovery.enabledZoneAware", havingValue = "true")
  public ZoneAwareServiceInstanceFilter zoneAwareServiceInstanceFilter(Registration registration,
      ZoneAwareFilterAdapter adapter, Environment environment) {
    return new ZoneAwareServiceInstanceFilter(registration, adapter, environment);
  }

  @Bean
  @ConditionalOnMissingBean(AffinityTagServiceInstanceFilter.class)
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.discovery.enabledAffinityTag", havingValue = "true")
  public AffinityTagServiceInstanceFilter affinityTagServiceInstanceFilter(Registration registration,
      AffinityTagFilterAdapter adapter, Environment environment) {
    return new AffinityTagServiceInstanceFilter(registration, adapter, environment);
  }
}
