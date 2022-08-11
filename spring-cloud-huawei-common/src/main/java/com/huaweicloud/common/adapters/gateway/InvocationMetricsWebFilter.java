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

package com.huaweicloud.common.adapters.gateway;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.common.metrics.InvocationMetrics;

import reactor.core.publisher.Mono;

public class InvocationMetricsWebFilter implements OrderedWebFilter {
  private final InvocationMetrics invocationMetrics;

  private final GovernanceProperties governanceProperties;

  public InvocationMetricsWebFilter(InvocationMetrics invocationMetrics,
      GovernanceProperties governanceProperties) {
    this.invocationMetrics = invocationMetrics;
    this.governanceProperties = governanceProperties;
  }

  @Override
  public int getOrder() {
    return governanceProperties.getGateway().getInvocationMetrics().getOrder();
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    exchange.getAttributes().put(InvocationMetrics.CONTEXT_TIME, System.currentTimeMillis());
    exchange.getAttributes().put(InvocationMetrics.CONTEXT_OPERATION, buildOperation(exchange.getRequest()));
    return chain.filter(exchange).doOnSuccess(v -> postProcess(exchange, null))
        .doOnError(e -> postProcess(exchange, e));
  }

  private void postProcess(ServerWebExchange exchange, Throwable ex) {
    String operation = exchange.getAttribute(InvocationMetrics.CONTEXT_OPERATION);
    if (StringUtils.isEmpty(operation)) {
      return;
    }

    long start = exchange.getAttribute(InvocationMetrics.CONTEXT_TIME);
    if (ex != null || exchange.getResponse().getStatusCode().is5xxServerError()) {
      this.invocationMetrics.recordFailedCall(operation,
          System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
      return;
    }
    this.invocationMetrics.recordSuccessfulCall(operation,
        System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
  }

  private String buildOperation(ServerHttpRequest request) {
    StringBuilder sb = new StringBuilder();
    sb.append(request.getMethod());
    sb.append(" ");
    sb.append(request.getURI().getPath());
    return sb.toString();
  }
}
