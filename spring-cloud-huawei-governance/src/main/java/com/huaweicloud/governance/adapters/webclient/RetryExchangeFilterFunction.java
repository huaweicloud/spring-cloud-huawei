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

import java.util.Optional;

import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.adapters.loadbalancer.RetryContext;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import reactor.core.publisher.Mono;

public class RetryExchangeFilterFunction implements ExchangeFilterFunction, Ordered {
  private final RetryHandler retryHandler;

  private final GovernanceProperties governanceProperties;

  public RetryExchangeFilterFunction(RetryHandler retryHandler,
      GovernanceProperties governanceProperties) {
    this.retryHandler = retryHandler;
    this.governanceProperties = governanceProperties;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    GovernanceRequestExtractor governanceRequest = WebClientUtils.createGovernanceRequest(request);

    Mono<ClientResponse> toRun = Mono.defer(() -> next.exchange(request));

    return addRetry(request, governanceRequest, toRun);
  }

  private Mono<ClientResponse> addRetry(ClientRequest request, GovernanceRequestExtractor governanceRequest,
      Mono<ClientResponse> toRun) {
    Retry retry = retryHandler.getActuator(governanceRequest);
    if (retry == null) {
      return toRun;
    }

    Optional<Object> invocationContext = request.attribute(InvocationContextHolder.ATTRIBUTE_KEY);
    if (invocationContext.isPresent()) {
      RetryPolicy retryPolicy = retryHandler.matchPolicy(governanceRequest);
      RetryContext retryContext = new RetryContext(retryPolicy.getRetryOnSame());
      ((InvocationContext) invocationContext.get()).putLocalContext(RetryContext.RETRY_CONTEXT, retryContext);
    }
    return toRun.transformDeferred(RetryOperator.of(retry));
  }

  @Override
  public int getOrder() {
    return governanceProperties.getWebclient().getRetry().getOrder();
  }
}
