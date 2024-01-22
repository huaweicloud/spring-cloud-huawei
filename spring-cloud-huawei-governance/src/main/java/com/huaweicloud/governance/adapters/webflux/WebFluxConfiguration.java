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

package com.huaweicloud.governance.adapters.webflux;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.IdentifierRateLimitingHandler;
import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

@Configuration
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class WebFluxConfiguration {
  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBFLUX_RATE_LIMITING_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public WebFilter rateLimitingWebFilter(RateLimitingHandler rateLimitingHandler,
      GovernanceProperties governanceProperties) {
    return new RateLimitingWebFilter(rateLimitingHandler, governanceProperties);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBFLUX_BULKHEAD_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public WebFilter bulkheadWebFilter(BulkheadHandler bulkheadHandler,
      GovernanceProperties governanceProperties) {
    return new BulkheadWebFilter(bulkheadHandler, governanceProperties);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBFLUX_CIRCUIT_BREAKER_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public WebFilter circuitBreakerWebFilter(CircuitBreakerHandler circuitBreakerHandler,
      GovernanceProperties governanceProperties) {
    return new CircuitBreakerWebFilter(circuitBreakerHandler, governanceProperties);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBFLUX_IDENTIFIER_RATE_LIMITING_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public WebFilter identifierRateLimitingWebFilter(IdentifierRateLimitingHandler identifierRateLimitingHandler,
      GovernanceProperties governanceProperties) {
    return new IdentifierRateLimitingWebFilter(identifierRateLimitingHandler, governanceProperties);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBFLUX_CONTEXT_MAPPER_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public WebFilter contextMapperWebFilter(@Qualifier("contextMapperHandler") MapperHandler mapperHandler) {
    return new ContextMapperWebFilter(mapperHandler);
  }
}
