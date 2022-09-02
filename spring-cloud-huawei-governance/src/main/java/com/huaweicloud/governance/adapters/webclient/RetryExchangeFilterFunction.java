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
package com.huaweicloud.governance.adapters.webclient;

import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import reactor.core.publisher.Mono;

public class RetryExchangeFilterFunction implements ExchangeFilterFunction, Ordered {
  private final RetryHandler retryHandler;

  public RetryExchangeFilterFunction(RetryHandler retryHandler) {
    this.retryHandler = retryHandler;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    GovernanceRequest governanceRequest = createGovernanceRequest(request);

    Mono<ClientResponse> toRun = next.exchange(request);

    return addRetry(request, governanceRequest, toRun);
  }

  private GovernanceRequest createGovernanceRequest(ClientRequest request) {
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setHeaders(request.headers().toSingleValueMap());
    governanceRequest.setUri(request.url().getPath());
    governanceRequest.setMethod(request.method().name());
    return governanceRequest;
  }

  private Mono<ClientResponse> addRetry(ClientRequest request, GovernanceRequest governanceRequest,
      Mono<ClientResponse> toRun) {
    Retry retry = retryHandler.getActuator(governanceRequest);
    if (retry == null) {
      return toRun;
    }

    return toRun.transformDeferred(RetryOperator.of(retry));
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
