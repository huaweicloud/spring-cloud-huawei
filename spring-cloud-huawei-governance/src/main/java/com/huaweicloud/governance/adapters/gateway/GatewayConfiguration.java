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

package com.huaweicloud.governance.adapters.gateway;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.IdentifierRateLimitingHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.gateway.config.conditional.ConditionalOnEnabledFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.governance.authentication.consumer.RSAConsumerTokenManager;

@Configuration
@ConditionalOnClass(name = {"org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory"})
@ConditionalOnProperty(value = "spring.cloud.servicecomb.gateway.governance.enabled",
    havingValue = "true", matchIfMissing = true)
public class GatewayConfiguration {
  @Bean
  @ConditionalOnEnabledFilter
  public GovernanceGatewayFilterFactory governanceGatewayFilterFactory(CircuitBreakerHandler circuitBreakerHandler,
      BulkheadHandler bulkheadHandler) {
    return new GovernanceGatewayFilterFactory(circuitBreakerHandler, bulkheadHandler);
  }

  @Bean
  @ConditionalOnEnabledFilter
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.gateway.instanceIsolation.enabled",
      havingValue = "true", matchIfMissing = true)
  public InstanceIsolationGlobalFilter instanceIsolationGlobalFilter(InstanceIsolationHandler handler) {
    return new InstanceIsolationGlobalFilter(handler);
  }

  @Bean
  @ConditionalOnEnabledFilter
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.gateway.faultInjection.enabled",
      havingValue = "true", matchIfMissing = true)
  public FaultInjectionGlobalFilter faultInjectionGlobalFilter(FaultInjectionHandler handler) {
    return new FaultInjectionGlobalFilter(handler);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED,
      havingValue = "true")
  public GatewayAddTokenContext gatewayAddTokenContext(RSAConsumerTokenManager authenticationTokenManager) {
    return new GatewayAddTokenContext(authenticationTokenManager);
  }

  @Bean
  @ConditionalOnEnabledFilter
  @ConditionalOnProperty(value = GovernanceProperties.GATEWAY_IDENTIFIER_RATE_LIMITING_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public WebFilter identifierRateLimitingWebFilter(IdentifierRateLimitingHandler identifierRateLimitingHandler,
      GovernanceProperties governanceProperties) {
    return new IdentifierRateLimitingWebFilter(identifierRateLimitingHandler, governanceProperties);
  }
}
