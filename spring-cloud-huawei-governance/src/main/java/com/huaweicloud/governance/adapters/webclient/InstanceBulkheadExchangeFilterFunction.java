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

import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import reactor.core.publisher.Mono;

public class InstanceBulkheadExchangeFilterFunction implements ExchangeFilterFunction, Ordered {
  private final InstanceBulkheadHandler bulkheadHandler;

  private final GovernanceProperties governanceProperties;

  public InstanceBulkheadExchangeFilterFunction(InstanceBulkheadHandler bulkheadHandler,
      GovernanceProperties governanceProperties) {
    this.bulkheadHandler = bulkheadHandler;
    this.governanceProperties = governanceProperties;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    GovernanceRequestExtractor governanceRequest = WebClientUtils.createGovernanceRequest(request);

    Mono<ClientResponse> toRun = Mono.defer(() -> next.exchange(request));

    return addBulkhead(governanceRequest, toRun);
  }

  private Mono<ClientResponse> addBulkhead(GovernanceRequestExtractor governanceRequest,
      Mono<ClientResponse> toRun) {
    Bulkhead bulkhead = bulkheadHandler.getActuator(governanceRequest);
    if (bulkhead == null) {
      return toRun;
    }

    return toRun.transformDeferred(BulkheadOperator.of(bulkhead));
  }

  @Override
  public int getOrder() {
    return governanceProperties.getWebclient().getInstanceBulkhead().getOrder();
  }
}
