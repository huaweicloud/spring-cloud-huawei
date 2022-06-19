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

package com.huaweicloud.governance.adapters.gateway;

import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.PullInstanceEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.governance.SpringCloudInvocationContext;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.reactor.circuitbreaker.operator.CircuitBreakerOperator;
import reactor.core.publisher.Mono;

public class InstanceIsolationGlobalFilter implements GlobalFilter, Ordered {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIsolationGlobalFilter.class);

  private final InstanceIsolationHandler isolationHandler;

  public InstanceIsolationGlobalFilter(InstanceIsolationHandler isolationHandler) {
    this.isolationHandler = isolationHandler;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    GovernanceRequest governanceRequest = createGovernanceRequest(exchange);

    try {
      SpringCloudInvocationContext.setInvocationContext();
      Mono<Void> toRun = chain.filter(exchange);
      toRun = addInstanceIsolation(governanceRequest, toRun);
      return toRun;
    } finally {
      SpringCloudInvocationContext.removeInvocationContext();
    }
  }

  private GovernanceRequest createGovernanceRequest(ServerWebExchange exchange) {
    GovernanceRequest request = new GovernanceRequest();
    request.setHeaders(exchange.getRequest().getHeaders().toSingleValueMap());
    request.setMethod(exchange.getRequest().getMethodValue());
    request.setUri(exchange.getRequest().getURI().getPath());

    Response<ServiceInstance> response = exchange.getAttribute(
        ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
    if (response.hasServer()) {
      request.setServiceName(response.getServer().getServiceId());
      request.setInstanceId(response.getServer().getInstanceId());
    }
    return request;
  }

  private Mono<Void> addInstanceIsolation(GovernanceRequest governanceRequest,
      Mono<Void> toRun) {
    CircuitBreaker circuitBreaker = isolationHandler.getActuator(governanceRequest);
    Mono<Void> mono = toRun;
    if (circuitBreaker != null) {
      mono = toRun.transformDeferred(CircuitBreakerOperator.of(circuitBreaker))
          .onErrorResume(CallNotPermittedException.class, (t) -> {
            LOGGER.warn("instance isolated [{}]", governanceRequest.getInstanceId());
            EventManager.post(new PullInstanceEvent());
            return Mono.error(new ResponseStatusException(HttpStatus.SERVICE_UNAVAILABLE,
                "instance isolated.", t));
          })
          .doOnError(e -> {
            LOGGER.warn("instance isolation got error.", e);
          });
    }
    return mono;
  }

  @Override
  public int getOrder() {
    return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 100;
  }
}
