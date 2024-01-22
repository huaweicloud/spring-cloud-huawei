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

import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import reactor.core.publisher.Mono;

public class InstanceBulkheadGlobalFilter implements GlobalFilter, Ordered {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceBulkheadGlobalFilter.class);

  private final InstanceBulkheadHandler instanceBulkheadHandler;

  public InstanceBulkheadGlobalFilter(InstanceBulkheadHandler instanceBulkheadHandler) {
    this.instanceBulkheadHandler = instanceBulkheadHandler;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    GovernanceRequestExtractor governanceRequest = GatewayUtils.createConsumerGovernanceRequest(exchange);

    Mono<Void> toRun = chain.filter(exchange);

    toRun = addInstanceBulkhead(governanceRequest, toRun);
    return toRun;
  }

  private Mono<Void> addInstanceBulkhead(GovernanceRequestExtractor governanceRequest,
      Mono<Void> toRun) {
    Bulkhead bulkhead = instanceBulkheadHandler.getActuator(governanceRequest);
    Mono<Void> mono = toRun;
    if (bulkhead != null) {
      mono = toRun.transformDeferred(BulkheadOperator.of(bulkhead))
          .onErrorResume(BulkheadFullException.class, (t) -> {
            LOGGER.error("bulkhead is full [{}]", governanceRequest.instanceId());
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "bulkhead is full.", t));
          });
    }
    return mono;
  }

  @Override
  public int getOrder() {
    return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 200;
  }
}
