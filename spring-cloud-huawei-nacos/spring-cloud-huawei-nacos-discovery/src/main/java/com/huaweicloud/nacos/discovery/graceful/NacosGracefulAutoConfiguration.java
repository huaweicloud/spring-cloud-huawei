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

package com.huaweicloud.nacos.discovery.graceful;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.nacos.discovery.ConditionalOnNacosDiscoveryEnabled;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.registry.NacosAutoServiceRegistration;
import com.huaweicloud.nacos.discovery.registry.NacosRegistration;
import com.huaweicloud.nacos.discovery.registry.NacosServiceRegistry;

@Configuration
@ConditionalOnNacosDiscoveryEnabled
@ConditionalOnClass(name = {"org.springframework.boot.actuate.endpoint.annotation.Endpoint"})
public class NacosGracefulAutoConfiguration {
  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.NACOS_GRASEFUL_UPPER_DOWN, havingValue = "true")
  public NacosGracefulEndpoint nacosGracefulEndpoint(NacosServiceRegistry nacosServiceRegistry,
      NacosRegistration nacosRegistration, NacosAutoServiceRegistration nacosAutoServiceRegistration,
      NacosDiscoveryProperties nacosDiscoveryProperties) {
    return new NacosGracefulEndpoint(nacosServiceRegistry, nacosRegistration, nacosAutoServiceRegistration,
        nacosDiscoveryProperties);
  }
}
