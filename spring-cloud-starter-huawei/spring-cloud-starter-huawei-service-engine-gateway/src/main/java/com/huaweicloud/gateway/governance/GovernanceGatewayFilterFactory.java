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

package com.huaweicloud.gateway.governance;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.reset;

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

public class GovernanceGatewayFilterFactory extends
    AbstractGatewayFilterFactory<GovernanceGatewayFilterFactory.Config> {

  private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceGatewayFilterFactory.class);

  public GovernanceGatewayFilterFactory() {
    super(Config.class);
  }

  @Autowired
  private RateLimitingHandler rateLimitingHandler;

  @Autowired
  private CircuitBreakerHandler circuitBreakerHandler;

  @Autowired
  private BulkheadHandler bulkheadHandler;

  @Autowired
  private RetryHandler retryHandler;

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
      if (retry != null) {
        toRun = toRun.transform(RetryOperator.of(retry))
            .doOnSuccess(v -> {
              if (exchange.getResponse().getRawStatusCode() != null) {
                if (retry.context().onResult(exchange.getResponse().getRawStatusCode())) {
                  exchange.getResponse().setStatusCode(null);
                  reset(exchange);
                  throw new RetryException();
                }
              }
            }).retryWhen(reactor.util.retry.Retry.withThrowable(reactor.retry.Retry.anyOf(RetryException.class)));
      }
      return toRun;
    }

    private Mono<Void> addBulkhead(GovernanceRequest governanceRequest, Mono<Void> toRun) {
      Bulkhead bulkhead = bulkheadHandler.getActuator(governanceRequest);
      if (bulkhead != null) {
        toRun = toRun.transform(BulkheadOperator.of(bulkhead))
            .onErrorResume(BulkheadFullException.class, (t) -> {
              LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
                  t.getMessage());
              return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                  "bulkhead is full and does not permit further calls.", t));
            });
      }
      return toRun;
    }

    private Mono<Void> addCircuitBreaker(ServerWebExchange exchange, GovernanceRequest governanceRequest,
        Mono<Void> toRun) {
      CircuitBreaker circuitBreaker = circuitBreakerHandler.getActuator(governanceRequest);
      if (circuitBreaker != null) {
        toRun = toRun.transform(CircuitBreakerOperator.of(circuitBreaker))
            .doOnSuccess(v -> {
              if (exchange.getResponse().getStatusCode() != null
                  && exchange.getResponse().getStatusCode().is5xxServerError()) {
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
      return toRun;
    }

    private Mono<Void> addRateLimiter(GovernanceRequest governanceRequest, Mono<Void> toRun) {
      RateLimiter rateLimiter = rateLimitingHandler.getActuator(governanceRequest);
      if (rateLimiter != null) {
        toRun = toRun.transform(RateLimiterOperator.of(rateLimiter))
            .onErrorResume(RequestNotPermitted.class, (t) -> {
              LOGGER.warn("the request is rate limit by policy : {}",
                  t.getMessage());
              return Mono.error(new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "rate limited.", t));
            });
      }
      return toRun;
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
