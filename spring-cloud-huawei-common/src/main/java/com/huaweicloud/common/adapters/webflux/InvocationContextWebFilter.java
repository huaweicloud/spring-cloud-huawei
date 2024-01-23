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

package com.huaweicloud.common.adapters.webflux;

import org.apache.commons.lang.StringUtils;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.context.InvocationStage;

import reactor.core.publisher.Mono;

public class InvocationContextWebFilter implements OrderedWebFilter {
  private final static String INVOCATION_CONTEXT_ENABLED = "spring.cloud.servicecomb.context.web-flux.enabled";

  private final ContextProperties contextProperties;

  private final Environment env;

  public InvocationContextWebFilter(ContextProperties contextProperties, Environment env) {
    this.contextProperties = contextProperties;
    this.env = env;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    InvocationContext context;
    if (env.getProperty(INVOCATION_CONTEXT_ENABLED, boolean.class, true)) {
      context = InvocationContextHolder.deserialize(
          exchange.getRequest().getHeaders().getFirst(InvocationContextHolder.SERIALIZE_KEY));
    } else {
      context = new InvocationContext();
    }

    // copy external headers to context
    contextProperties.getHeaderContextMapper()
        .forEach((k, v) -> {
          if (!StringUtils.isEmpty(exchange.getRequest().getHeaders().getFirst(k))) {
            context.putContext(v, exchange.getRequest().getHeaders().getFirst(k));
          }
        });
    contextProperties.getQueryContextMapper()
        .forEach((k, v) -> {
          if (!StringUtils.isEmpty(exchange.getRequest().getQueryParams().getFirst(k))) {
            context.putContext(v, exchange.getRequest().getQueryParams().getFirst(k));
          }
        });

    // copy or generate trace id
    if (context.getContext(InvocationContext.CONTEXT_TRACE_ID) == null) {
      context.putContext(InvocationContext.CONTEXT_TRACE_ID, InvocationContext.generateTraceId());
    }

    exchange.getAttributes().put(InvocationContextHolder.ATTRIBUTE_KEY, context);

    InvocationStage stage = context.getInvocationStage();
    stage.begin(buildId(exchange.getRequest(), context));

    return chain.filter(exchange).doOnSuccess(v -> postProcess(exchange, null))
        .doOnError(e -> postProcess(exchange, e));
  }

  private void postProcess(ServerWebExchange exchange, Throwable e) {
    InvocationStage stage = ((InvocationContext) exchange
        .getAttribute(InvocationContextHolder.ATTRIBUTE_KEY))
        .getInvocationStage();
    if (e instanceof ResponseStatusException) {
      stage.finish(((ResponseStatusException) e).getRawStatusCode());
      return;
    }
    stage.finish(exchange.getResponse().getStatusCode() == null ? -1 : exchange.getResponse().getStatusCode().value());
  }

  private String buildId(ServerHttpRequest request, InvocationContext context) {
    if (contextProperties.isUseContextOperationForMetrics()) {
      if (context.getContext(InvocationContext.CONTEXT_OPERATION_ID) != null) {
        return context.getContext(InvocationContext.CONTEXT_OPERATION_ID);
      }
      String id = buildOperation(request);
      context.putContext(InvocationContext.CONTEXT_OPERATION_ID, id);
      return id;
    }
    return buildOperation(request);
  }

  private String buildOperation(ServerHttpRequest request) {
    StringBuilder sb = new StringBuilder();
    sb.append(request.getMethod());
    sb.append(" ");
    sb.append(request.getURI().getPath());
    return sb.toString();
  }
}
