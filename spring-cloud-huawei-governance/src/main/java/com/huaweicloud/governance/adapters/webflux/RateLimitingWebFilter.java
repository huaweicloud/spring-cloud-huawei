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

import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import reactor.core.publisher.Mono;

public class RateLimitingWebFilter implements OrderedWebFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingWebFilter.class);

  private final RateLimitingHandler rateLimitingHandler;

  private final GovernanceProperties governanceProperties;

  public RateLimitingWebFilter(RateLimitingHandler rateLimitingHandler, GovernanceProperties governanceProperties) {
    this.rateLimitingHandler = rateLimitingHandler;
    this.governanceProperties = governanceProperties;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    GovernanceRequestExtractor governanceRequest = WebFluxUtils.createProviderGovernanceRequest(exchange);

    Mono<Void> toRun = chain.filter(exchange);

    return addRateLimiter(governanceRequest, toRun);
  }

  private Mono<Void> addRateLimiter(GovernanceRequestExtractor governanceRequest, Mono<Void> toRun) {
    RateLimiter rateLimiter = rateLimitingHandler.getActuator(governanceRequest);
    Mono<Void> mono = toRun;
    if (rateLimiter != null) {
      mono = toRun.transform(RateLimiterOperator.of(rateLimiter))
          .onErrorResume(RequestNotPermitted.class, (t) -> {
            LOGGER.warn("the request is rate limited by policy : {}",
                t.getMessage());
            return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "rate limited.", t));
          });
    }
    return mono;
  }

  @Override
  public int getOrder() {
    return governanceProperties.getGateway().getRateLimiting().getOrder();
  }
}
