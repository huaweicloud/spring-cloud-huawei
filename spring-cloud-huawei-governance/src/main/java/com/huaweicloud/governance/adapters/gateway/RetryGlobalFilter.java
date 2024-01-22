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

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

import java.net.URI;

import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.adapters.loadbalancer.RetryContext;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import reactor.core.publisher.Mono;

public class RetryGlobalFilter implements GlobalFilter, Ordered {

  public static final int RETRY_ORDER = ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER - 10;

  private final RetryHandler retryHandler;

  public RetryGlobalFilter(RetryHandler retryHandler) {
    this.retryHandler = retryHandler;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    GovernanceRequestExtractor governanceRequest = GatewayUtils.createConsumerGovernanceRequest(exchange);

    Mono<Void> toRun = chain.filter(exchange);

    return addRetry(exchange, governanceRequest, toRun);
  }

  private Mono<Void> addRetry(ServerWebExchange exchange, GovernanceRequestExtractor governanceRequest,
      Mono<Void> toRun) {
    Retry retry = retryHandler.getActuator(governanceRequest);
    if (retry == null) {
      return toRun;
    }
    InvocationContext invocationContext = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
    if (invocationContext != null) {
      RetryPolicy retryPolicy = retryHandler.matchPolicy(governanceRequest);
      RetryContext retryContext = new RetryContext(retryPolicy.getRetryOnSame());
      invocationContext.putLocalContext(RetryContext.RETRY_CONTEXT, retryContext);
    }
    URI originUrl = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
    return Mono.defer(() -> Mono.fromRunnable(() -> {
          int iteration = exchange.getAttributeOrDefault(RetryContext.RETRY_ITERATION, 0);
          if (iteration > 0) {
            exchange.getAttributes().put(GATEWAY_REQUEST_URL_ATTR, originUrl);
            reset(exchange);
          }
          exchange.getAttributes().put(RetryContext.RETRY_ITERATION, iteration + 1);
        })).then(toRun).then(
            Mono.defer(() -> Mono.just(exchange.getResponse().getStatusCode() == null ? 0
                : exchange.getResponse().getStatusCode().value())))
        .transformDeferred(RetryOperator.of(retry))
        .then();
  }

  private void reset(ServerWebExchange exchange) {
    ServerWebExchangeUtils.reset(exchange);
  }

  @Override
  public int getOrder() {
    return RETRY_ORDER;
  }
}
