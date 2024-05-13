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

package com.huaweicloud.zookeeper.discovery.discovery;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.zookeeper.discovery.ConditionalOnZookeeperDiscoveryEnabled;
import com.huaweicloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import com.huaweicloud.zookeeper.discovery.ZookeeperServiceInstance;

@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnZookeeperDiscoveryEnabled
public class ZookeeperDiscoveryAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public ZookeeperDiscoveryClient zookeeperDiscoveryClient(
      ServiceDiscovery<ZookeeperServiceInstance> serviceDiscovery, ZookeeperDiscoveryProperties discoveryProperties) {
    return new ZookeeperDiscoveryClient(serviceDiscovery, discoveryProperties);
  }

  @Bean
  @ConditionalOnMissingBean
  @Conditional(ZookeeperDiscoveryHeartBeatCondition.class)
  public ZookeeperDiscoveryHeartBeatTask zookeeperDiscoveryHeartBeatPublisher(
      ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
    return new ZookeeperDiscoveryHeartBeatTask(zookeeperDiscoveryProperties);
  }

  private static class ZookeeperDiscoveryHeartBeatCondition extends AnyNestedCondition {
    ZookeeperDiscoveryHeartBeatCondition() {
      super(ConfigurationPhase.REGISTER_BEAN);
    }

    /**
     * Spring Cloud Gateway HeartBeat .
     */
    @ConditionalOnProperty(value = "spring.cloud.gateway.discovery.locator.enabled")
    static class GatewayLocatorHeartBeatEnabled { }

    /**
     * Spring Boot Admin HeartBeat .
     */
    @ConditionalOnBean(type = "de.codecentric.boot.admin.server.cloud.discovery.InstanceDiscoveryListener")
    static class SpringBootAdminHeartBeatEnabled { }

    /**
     * Zookeeper HeartBeat .
     */
    @ConditionalOnProperty(value = "spring.cloud.zookeeper.discovery.heart-beat.enabled")
    static class ZookeeperDiscoveryHeartBeatEnabled { }
  }
}
