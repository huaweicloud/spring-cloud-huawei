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

package com.huaweicloud.zookeeper.discovery.discovery.reactive;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.cloud.client.ConditionalOnReactiveDiscoveryEnabled;
import org.springframework.cloud.client.ReactiveCommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.composite.reactive.ReactiveCompositeDiscoveryClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import com.huaweicloud.zookeeper.discovery.discovery.ZookeeperDiscoveryAutoConfiguration;
import com.huaweicloud.zookeeper.discovery.discovery.ZookeeperDiscoveryClient;

@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnReactiveDiscoveryEnabled
@ConditionalOnZookeeperDiscoveryEnabled
@AutoConfigureAfter({ZookeeperDiscoveryAutoConfiguration.class,
    ReactiveCompositeDiscoveryClientAutoConfiguration.class})
@AutoConfigureBefore({ReactiveCommonsClientAutoConfiguration.class})
public class ZookeeperReactiveDiscoveryClientConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ZookeeperReactiveDiscoveryClient zookeeperReactiveDiscoveryClient(ZookeeperDiscoveryClient discoveryClient) {
    return new ZookeeperReactiveDiscoveryClient(discoveryClient);
  }
}
