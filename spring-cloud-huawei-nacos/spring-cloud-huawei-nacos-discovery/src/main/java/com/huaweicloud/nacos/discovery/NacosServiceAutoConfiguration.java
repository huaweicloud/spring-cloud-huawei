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

package com.huaweicloud.nacos.discovery;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.nacos.discovery.discovery.NacosCrossGroupProperties;
import com.huaweicloud.nacos.discovery.manager.NamingServiceMasterManager;
import com.huaweicloud.nacos.discovery.manager.NamingServiceStandbyManager;

@Configuration
@ConditionalOnNacosDiscoveryEnabled
public class NacosServiceAutoConfiguration {
  @Bean
  @ConfigurationProperties("spring.cloud.nacos.discovery")
  public NacosDiscoveryProperties nacosDiscoveryProperties() {
    return new NacosDiscoveryProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.cross-group")
  public NacosCrossGroupProperties nacosCrossGroupServiceConfig() {
    return new NacosCrossGroupProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  public NamingServiceMasterManager namingServiceMasterManager(NacosDiscoveryProperties properties) {
    return new NamingServiceMasterManager(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(value = NacosConst.MASTER_STANDBY_ENABLED, havingValue = "true")
  public NamingServiceStandbyManager namingServiceStandbyManager(NacosDiscoveryProperties properties) {
    return new NamingServiceStandbyManager(properties);
  }
}
