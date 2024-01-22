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

package com.huaweicloud.common.adapters.gateway;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.ContextProperties;

@Configuration
@ConditionalOnClass(name = {"org.springframework.cloud.gateway.filter.GlobalFilter"})
@ConditionalOnProperty(value = ContextProperties.GATEWAY_CONTEXT_ENABLED,
    havingValue = "true", matchIfMissing = true)
public class GatewayConfiguration {
  @Bean
  public GlobalFilter serializeContextGlobalFilter() {
    return new SerializeContextGlobalFilter();
  }

  @Bean
  public GlobalFilter gatewayMetricsGlobalFilter() {
    return new GatewayMetricsGlobalFilter();
  }

  @Bean
  public GlobalFilter gatewayAddServiceNameContext(@Autowired(required = false) Registration registration) {
    return new GatewayAddServiceNameContext(registration);
  }
}
