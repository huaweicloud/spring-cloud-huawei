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
package com.huaweicloud.governance.adapters.webclient;

import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

public class InstanceIsolationExchangeFilterFunction implements ExchangeFilterFunction, Ordered {
  private final InstanceIsolationHandler isolationHandler;

  private final GovernanceProperties governanceProperties;

  public InstanceIsolationExchangeFilterFunction(InstanceIsolationHandler isolationHandler,
      GovernanceProperties governanceProperties) {
    this.isolationHandler = isolationHandler;
    this.governanceProperties = governanceProperties;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    GovernanceRequestExtractor governanceRequest = WebClientUtils.createGovernanceRequest(request);

    Mono<ClientResponse> toRun = Mono.defer(() -> next.exchange(request));

    return addInstanceIsolation(governanceRequest, toRun);
  }

  private Mono<ClientResponse> addInstanceIsolation(GovernanceRequestExtractor governanceRequest,
      Mono<ClientResponse> toRun) {
    CircuitBreaker circuitBreaker = isolationHandler.getActuator(governanceRequest);
    if (circuitBreaker == null) {
      return toRun;
    }

    return toRun.transformDeferred(CircuitBreakerOperator.of(circuitBreaker));
  }

  @Override
  public int getOrder() {
    return governanceProperties.getWebclient().getInstanceIsolation().getOrder();
  }
}
