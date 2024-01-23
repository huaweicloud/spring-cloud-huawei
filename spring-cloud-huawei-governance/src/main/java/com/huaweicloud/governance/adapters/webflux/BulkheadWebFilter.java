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
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import reactor.core.publisher.Mono;

public class BulkheadWebFilter implements OrderedWebFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(BulkheadWebFilter.class);

  private final BulkheadHandler bulkheadHandler;

  private final GovernanceProperties governanceProperties;

  public BulkheadWebFilter(BulkheadHandler bulkheadHandler, GovernanceProperties governanceProperties) {
    this.bulkheadHandler = bulkheadHandler;
    this.governanceProperties = governanceProperties;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    GovernanceRequestExtractor governanceRequest = WebFluxUtils.createProviderGovernanceRequest(exchange);

    Mono<Void> toRun = chain.filter(exchange);

    return addBulkhead(governanceRequest, toRun);
  }

  private Mono<Void> addBulkhead(GovernanceRequestExtractor governanceRequest, Mono<Void> toRun) {
    Bulkhead bulkhead = bulkheadHandler.getActuator(governanceRequest);
    Mono<Void> mono = toRun;
    if (bulkhead != null) {
      mono = toRun.transform(BulkheadOperator.of(bulkhead))
          .onErrorResume(BulkheadFullException.class, (t) -> {
            LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
                t.getMessage());
            return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                "bulkhead is full and does not permit further calls.", t));
          });
    }
    return mono;
  }

  @Override
  public int getOrder() {
    return governanceProperties.getGateway().getBulkhead().getOrder();
  }
}
