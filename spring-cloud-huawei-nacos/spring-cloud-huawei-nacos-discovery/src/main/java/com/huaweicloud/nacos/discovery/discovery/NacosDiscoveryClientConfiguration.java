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

package com.huaweicloud.nacos.discovery.discovery;

import com.huaweicloud.common.event.ClosedEventListener;
import com.huaweicloud.nacos.discovery.ConditionalOnNacosDiscoveryEnabled;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.NacosServiceAutoConfiguration;
import com.huaweicloud.nacos.discovery.NamingServiceManager;
import com.huaweicloud.nacos.discovery.watch.NacosServiceWatch;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.AnyNestedCondition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnNacosDiscoveryEnabled
@AutoConfigureAfter({ NacosServiceAutoConfiguration.class })
public class NacosDiscoveryClientConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public NacosDiscovery nacosDiscovery(NacosDiscoveryProperties discoveryProperties,
      NamingServiceManager namingServiceManager, NacosCrossGroupProperties crossGroupProperties) {
    return new NacosDiscovery(discoveryProperties, namingServiceManager, crossGroupProperties);
  }

  @Bean
  @ConditionalOnMissingBean
  public NacosDiscoveryClient nacosDiscoveryClient(NacosDiscovery nacosDiscovery) {
    return new NacosDiscoveryClient(nacosDiscovery);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.watch.enabled")
  public NacosServiceWatch nacosServiceWatch(NacosDiscoveryProperties discoveryProperties,
      NamingServiceManager namingServiceManager, ClosedEventListener closedEventListener) {
    return new NacosServiceWatch(discoveryProperties, namingServiceManager, closedEventListener);
  }

  @Bean
  @ConditionalOnMissingBean
  @Conditional(NacosDiscoveryHeartBeatCondition.class)
  public NacosDiscoveryHeartBeatTask nacosDiscoveryHeartBeatPublisher(NacosDiscoveryProperties nacosDiscoveryProperties) {
    return new NacosDiscoveryHeartBeatTask(nacosDiscoveryProperties);
  }

  private static class NacosDiscoveryHeartBeatCondition extends AnyNestedCondition {
    NacosDiscoveryHeartBeatCondition() {
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
     * Nacos HeartBeat .
     */
    @ConditionalOnProperty(value = "spring.cloud.nacos.discovery.heart-beat.enabled")
    static class NacosDiscoveryHeartBeatEnabled { }
  }
}
