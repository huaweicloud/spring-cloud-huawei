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

import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

public class CircuitBreakerWebFilter implements OrderedWebFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(CircuitBreakerWebFilter.class);

  private final CircuitBreakerHandler circuitBreakerHandler;

  private final GovernanceProperties governanceProperties;

  public CircuitBreakerWebFilter(CircuitBreakerHandler circuitBreakerHandler,
      GovernanceProperties governanceProperties) {
    this.circuitBreakerHandler = circuitBreakerHandler;
    this.governanceProperties = governanceProperties;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    GovernanceRequestExtractor governanceRequest = WebFluxUtils.createProviderGovernanceRequest(exchange);

    Mono<Void> toRun = chain.filter(exchange);

    return addCircuitBreaker(exchange, governanceRequest, toRun);
  }

  private Mono<Void> addCircuitBreaker(ServerWebExchange exchange, GovernanceRequestExtractor governanceRequest,
      Mono<Void> toRun) {
    CircuitBreaker circuitBreaker = circuitBreakerHandler.getActuator(governanceRequest);
    Mono<Void> mono = toRun;
    if (circuitBreaker != null) {
      mono = toRun.then(Mono.defer(() -> Mono.just(exchange.getResponse().getStatusCode() == null ? 0
              : exchange.getResponse().getStatusCode().value())))
          .transform(CircuitBreakerOperator.of(circuitBreaker))
          .then()
          .onErrorResume(CallNotPermittedException.class, (t) -> {
            LOGGER.warn("circuitBreaker is open by policy : {}",
                t.getMessage());
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "circuitBreaker is open.", t));
          });
    }
    return mono;
  }

  @Override
  public int getOrder() {
    return governanceProperties.getGateway().getCircuitBreaker().getOrder();
  }
}
