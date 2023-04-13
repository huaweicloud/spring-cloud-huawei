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

package com.huaweicloud.nacos;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.governance.authentication.MicroserviceInstanceService;
import com.huaweicloud.nacos.authentication.NacosInstanceService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnNacosDiscoveryEnabled
public class AnthenAutoConfiguration {
  @Bean
  @ConditionalOnExpression("${" + GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED + ":true}"
          + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED + ":true}")
  public MicroserviceInstanceService microserviceInstanceService(NacosDiscoveryProperties properties,
    NacosServiceDiscovery serviceDiscovery, NacosRegistration registration) {
    return new NacosInstanceService(properties, registration, serviceDiscovery);
  }
}
