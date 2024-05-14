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

package com.huaweicloud.zookeeper.discovery;

import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceDiscoveryBuilder;
import org.apache.curator.x.discovery.details.InstanceSerializer;
import org.apache.curator.x.discovery.details.JsonInstanceSerializer;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.ConditionalOnDiscoveryEnabled;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.zookeeper.discovery.discovery.ZookeeperDiscoveryAutoConfiguration;
import com.huaweicloud.zookeeper.discovery.registry.ZookeeperServiceRegistryAutoConfiguration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnDiscoveryEnabled
@ConditionalOnZookeeperDiscoveryEnabled
@AutoConfigureBefore({ZookeeperDiscoveryAutoConfiguration.class,
    ZookeeperServiceRegistryAutoConfiguration.class})
public class CuratorServiceDiscoveryAutoConfiguration {
  @Bean
  @ConfigurationProperties("spring.cloud.zookeeper.discovery")
  public ZookeeperDiscoveryProperties zookeeperDiscoveryProperties() {
    return new ZookeeperDiscoveryProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  public InstanceSerializer<ZookeeperServiceInstance> deprecatedInstanceSerializer() {
    return new JsonInstanceSerializer<>(ZookeeperServiceInstance.class);
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceDiscovery<ZookeeperServiceInstance> curatorServiceDiscovery(ZookeeperDiscoveryProperties properties,
      InstanceSerializer<ZookeeperServiceInstance> serializer, ObjectProvider<EnsembleProvider> optionalEnsembleProvider,
      ObjectProvider<ServiceCuratorFrameworkCustomizer> optionalCustomizerProvider) {
    return ServiceDiscoveryBuilder.builder(ZookeeperServiceInstance.class)
        .client(CuratorUtils.createCuratorFramework(properties, optionalCustomizerProvider::orderedStream,
            optionalEnsembleProvider::getIfAvailable))
        .basePath(properties.getRoot())
        .serializer(serializer)
        .build();
  }
}
