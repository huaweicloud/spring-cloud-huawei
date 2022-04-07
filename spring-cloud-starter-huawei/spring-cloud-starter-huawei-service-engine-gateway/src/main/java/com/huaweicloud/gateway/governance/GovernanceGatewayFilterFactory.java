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

package com.huaweicloud.gateway.governance;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.reset;

import javax.ws.rs.core.Response.Status.Family;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.cloud.gateway.support.HasRouteId;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.governance.SpringCloudInvocationContext;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.github.resilience4j.reactor.bulkhead.operator.BulkheadOperator;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import io.github.resilience4j.reactor.ratelimiter.operator.RateLimiterOperator;
import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import reactor.core.publisher.Mono;

public class GovernanceGatewayFilterFactory
    extends AbstractGatewayFilterFactory<GovernanceGatewayFilterFactory.Config> {
  private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceGatewayFilterFactory.class);

  private RateLimitingHandler rateLimitingHandler;

  private CircuitBreakerHandler circuitBreakerHandler;

  private BulkheadHandler bulkheadHandler;

  private RetryHandler retryHandler;

  @Autowired
  public GovernanceGatewayFilterFactory(RateLimitingHandler rateLimitingHandler, CircuitBreakerHandler circuitBreakerHandler, BulkheadHandler bulkheadHandler, RetryHandler retryHandler) {
    super(Config.class);
    this.rateLimitingHandler = rateLimitingHandler;
    this.circuitBreakerHandler = circuitBreakerHandler;
    this.bulkheadHandler = bulkheadHandler;
    this.retryHandler = retryHandler;
  }

  @Override
  public GatewayFilter apply(Config config) {
    return new GovernanceGatewayFilter();
  }

  @Override
  public String name() {
    return "governance";
  }

  class GovernanceGatewayFilter implements GatewayFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {

      GovernanceRequest governanceRequest = createGovernanceRequest(exchange);

      try {
        SpringCloudInvocationContext.setInvocationContext();
        Mono<Void> toRun = chain.filter(exchange);

        toRun = addRetry(exchange, governanceRequest, toRun);
        toRun = addCircuitBreaker(exchange, governanceRequest, toRun);
        toRun = addBulkhead(governanceRequest, toRun);
        toRun = addRateLimiter(governanceRequest, toRun);

        return toRun;
      } finally {
        SpringCloudInvocationContext.removeInvocationContext();
      }
    }

    private Mono<Void> addRetry(ServerWebExchange exchange, GovernanceRequest governanceRequest, Mono<Void> toRun) {
      Retry retry = retryHandler.getActuator(governanceRequest);
      Mono<Void> mono = toRun;
      if (retry != null) {
        mono = toRun.transform(RetryOperator.of(retry))
                .doOnSuccess(v -> {
                  if (exchange.getResponse().getRawStatusCode() != null) {
                    if (retry.context().onResult(exchange.getResponse().getRawStatusCode())) {
                      exchange.getResponse().setStatusCode(null);
                      reset(exchange);
                      throw new RetryException();
                    }
                  }
                }).retryWhen(reactor.util.retry.Retry.indefinitely().filter(e -> e instanceof RetryException));
      }
      return mono;
    }

    private Mono<Void> addBulkhead(GovernanceRequest governanceRequest, Mono<Void> toRun) {
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

    private Mono<Void> addCircuitBreaker(ServerWebExchange exchange, GovernanceRequest governanceRequest,
        Mono<Void> toRun) {
      CircuitBreaker circuitBreaker = circuitBreakerHandler.getActuator(governanceRequest);
      Mono<Void> mono = toRun;
      if (circuitBreaker != null) {
        mono = toRun.transform(CircuitBreakerOperator.of(circuitBreaker))
            .doOnSuccess(v -> {
              if (exchange.getResponse().getStatusCode() != null
                  && Family.familyOf(exchange.getResponse().getStatusCode().value()) == Family.SERVER_ERROR) {
                exchange.getResponse().setStatusCode(null);
                reset(exchange);
                throw CallNotPermittedException.createCallNotPermittedException(circuitBreaker);
              }
            })
            .onErrorResume(CallNotPermittedException.class, (t) -> {
              LOGGER.warn("circuitBreaker is open by policy : {}",
                  t.getMessage());
              return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                  "bulkhead is full and does not permit further calls.", t));
            });
      }
      return mono;
    }

    private Mono<Void> addRateLimiter(GovernanceRequest governanceRequest, Mono<Void> toRun) {
      RateLimiter rateLimiter = rateLimitingHandler.getActuator(governanceRequest);
      Mono<Void> mono = toRun;
      if (rateLimiter != null) {
        mono = toRun.transform(RateLimiterOperator.of(rateLimiter))
            .onErrorResume(RequestNotPermitted.class, (t) -> {
              LOGGER.warn("the request is rate limit by policy : {}",
                  t.getMessage());
              return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "rate limited.", t));
            });
      }
      return mono;
    }

    private GovernanceRequest createGovernanceRequest(ServerWebExchange exchange) {
      GovernanceRequest request = new GovernanceRequest();
      request.setHeaders(exchange.getRequest().getHeaders().toSingleValueMap());
      request.setMethod(exchange.getRequest().getMethodValue());
      request.setUri(exchange.getRequest().getURI().getPath());
      return request;
    }
  }

  public static class Config implements HasRouteId {
    private String routeId;

    @Override
    public void setRouteId(String routeId) {
      this.routeId = routeId;
    }
    @Override
    public String getRouteId() {
      return routeId;
    }
  }

  public static class RetryException extends RuntimeException {
    static final long serialVersionUID = -1;

    public RetryException() {
      super();
    }
  }
}
