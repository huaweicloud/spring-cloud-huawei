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

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.alibaba.cloud.nacos.ConditionalOnNacosDiscoveryEnabled;
import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.nacos.authentication.NacosAuthenticationAdapter;
import com.huaweicloud.nacos.registry.MetadataNacosRegistrationCustomizer;
import com.huaweicloud.nacos.loadbalancer.NacosAffinityTagFilterAdapter;
import com.huaweicloud.nacos.loadbalancer.NacosCanaryFilterAdapter;
import com.huaweicloud.nacos.loadbalancer.NacosZoneAwareFilterAdapter;

@Configuration
@ConditionalOnNacosDiscoveryEnabled
public class NacosAdaptersAutoConfiguration {
  @Bean
  @ConditionalOnExpression("${" + GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED + ":true}"
      + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED + ":true}")
  public NacosAuthenticationAdapter nacosAuthenticationAdapter(DiscoveryClient discoveryClient) {
    return new NacosAuthenticationAdapter(discoveryClient);
  }

  @Bean
  public NacosCanaryFilterAdapter nacosCanaryFilterAdapter() {
    return new NacosCanaryFilterAdapter();
  }

  @Bean
  public NacosZoneAwareFilterAdapter nacosZoneAwareFilterAdapter() {
    return new NacosZoneAwareFilterAdapter();
  }

  @Bean
  public NacosAffinityTagFilterAdapter nacosAffinityTagFilterAdapter() {
    return new NacosAffinityTagFilterAdapter();
  }

  @Bean
  public MetadataNacosRegistrationCustomizer metadataNacosRegistrationCustomizer() {
    return new MetadataNacosRegistrationCustomizer();
  }
}
