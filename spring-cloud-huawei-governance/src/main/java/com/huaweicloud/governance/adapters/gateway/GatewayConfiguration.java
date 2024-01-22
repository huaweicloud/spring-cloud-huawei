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

package com.huaweicloud.governance.adapters.gateway;

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

@Configuration
@ConditionalOnClass(name = {"org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory"})
@ConditionalOnProperty(value = GovernanceProperties.GATEWAY_GOVERNANCE_ENABLED,
    havingValue = "true", matchIfMissing = true)
public class GatewayConfiguration {
  @Bean
  @ConditionalOnEnabledFilter
  @ConditionalOnProperty(value = GovernanceProperties.GATEWAY_INSTANCE_ISOLATION_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public GlobalFilter instanceIsolationGlobalFilter(InstanceIsolationHandler handler) {
    return new InstanceIsolationGlobalFilter(handler);
  }

  @Bean
  @ConditionalOnEnabledFilter
  @ConditionalOnProperty(value = GovernanceProperties.GATEWAY_INSTANCE_BULKHEAD_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public GlobalFilter instanceBulkheadGlobalFilter(InstanceBulkheadHandler handler) {
    return new InstanceBulkheadGlobalFilter(handler);
  }

  @Bean
  @ConditionalOnEnabledFilter
  @ConditionalOnProperty(value = GovernanceProperties.GATEWAY_FAULT_INJECTION_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public GlobalFilter faultInjectionGlobalFilter(FaultInjectionHandler handler) {
    return new FaultInjectionGlobalFilter(handler);
  }

  @Bean
  @ConditionalOnEnabledFilter
  @ConditionalOnProperty(value = GovernanceProperties.GATEWAY_RETRY_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public GlobalFilter retryGlobalFilter(RetryHandler retryHandler) {
    return new RetryGlobalFilter(retryHandler);
  }
}
