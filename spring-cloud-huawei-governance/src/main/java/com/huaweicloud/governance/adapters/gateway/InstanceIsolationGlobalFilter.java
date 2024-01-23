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

import java.time.Duration;

import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.governance.event.InstanceIsolatedEvent;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

public class InstanceIsolationGlobalFilter implements GlobalFilter, Ordered {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIsolationGlobalFilter.class);

  private final InstanceIsolationHandler isolationHandler;

  public InstanceIsolationGlobalFilter(InstanceIsolationHandler isolationHandler) {
    this.isolationHandler = isolationHandler;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    GovernanceRequestExtractor governanceRequest = GatewayUtils.createConsumerGovernanceRequest(exchange);

    Mono<Void> toRun = chain.filter(exchange);

    CircuitBreakerPolicy circuitBreakerPolicy = isolationHandler.matchPolicy(governanceRequest);
    if (circuitBreakerPolicy != null && circuitBreakerPolicy.isForceOpen()) {
      return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "instance force isolated."));
    }
    if (circuitBreakerPolicy != null && !circuitBreakerPolicy.isForceClosed()) {
      toRun = addInstanceIsolation(exchange, governanceRequest, toRun);
    }
    return toRun;
  }

  private Mono<Void> addInstanceIsolation(ServerWebExchange exchange, GovernanceRequestExtractor governanceRequest,
      Mono<Void> toRun) {
    CircuitBreakerPolicy circuitBreakerPolicy = isolationHandler.matchPolicy(governanceRequest);
    if (circuitBreakerPolicy != null && circuitBreakerPolicy.isForceOpen()) {
      return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
          "Policy " + circuitBreakerPolicy.getName() + " forced open and deny requests"));
    }

    CircuitBreaker circuitBreaker = isolationHandler.getActuator(governanceRequest);
    Mono<Void> mono = toRun;
    if (circuitBreaker != null) {
      mono = toRun.then(Mono.defer(() -> Mono.just(exchange.getResponse().getStatusCode() == null ? 0
              : exchange.getResponse().getStatusCode().value())))
          .transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
          .then()
          .onErrorResume(CallNotPermittedException.class, (t) -> {
            LOGGER.error("instance isolated [{}], [{}]", governanceRequest.instanceId(), t.getMessage());
            EventManager.post(new InstanceIsolatedEvent(governanceRequest.instanceId(),
                Duration.parse(circuitBreakerPolicy.getWaitDurationInOpenState())));
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "instance isolated.", t));
          });
    }
    return mono;
  }

  @Override
  public int getOrder() {
    return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 100;
  }
}
