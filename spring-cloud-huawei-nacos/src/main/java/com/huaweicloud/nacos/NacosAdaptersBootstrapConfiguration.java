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
package com.huaweicloud.nacos;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosConfigManager;
import com.huaweicloud.nacos.config.SystemManagedPropertySourceLocator;

@Configuration
@ConditionalOnNacosDiscoveryEnabled
@ConditionalOnProperty(value = "spring.cloud.servicecomb.system.property.load.enabled", havingValue = "true",
    matchIfMissing = true)
public class NacosAdaptersBootstrapConfiguration {
  @Bean
  public SystemManagedPropertySourceLocator servicecombNacosPropertySourceLocator(
      NacosConfigManager nacosConfigManager) {
    return new SystemManagedPropertySourceLocator(nacosConfigManager);
  }
}
