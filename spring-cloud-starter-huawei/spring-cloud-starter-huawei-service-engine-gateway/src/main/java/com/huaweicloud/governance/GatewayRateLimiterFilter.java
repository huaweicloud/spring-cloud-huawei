/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.governance;

import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.web.server.ServerWebExchange;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import reactor.core.publisher.Mono;

public class GatewayRateLimiterFilter implements GlobalFilter {

  @Autowired
  private RateLimitingHandler rateLimitingHandler;

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    try {
      SpringCloudInvocationContext.setInvocationContext();
      RateLimiter rateLimiter = rateLimitingHandler.getActuator(getGovernanceRequest(exchange));
      if (rateLimiter != null) {
        return Mono.empty().transformDeferred(RateLimiterOperator.of(rateLimiter)).then(chain.filter(exchange));
      }
      return chain.filter(exchange);
    } finally {
      SpringCloudInvocationContext.removeInvocationContext();
    }
  }

  private static GovernanceRequest getGovernanceRequest(ServerWebExchange exchange) {
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setHeaders(exchange.getRequest().getHeaders().toSingleValueMap());
    governanceRequest.setMethod(exchange.getRequest().getMethodValue());
    governanceRequest.setUri(exchange.getRequest().getURI().getPath());
    return governanceRequest;
  }

}
