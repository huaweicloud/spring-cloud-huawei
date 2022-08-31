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

package com.huaweicloud.common.adapters.webflux;

import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

public class InvocationContextWebFilter implements OrderedWebFilter {
  private final ContextProperties contextProperties;

  public InvocationContextWebFilter(ContextProperties contextProperties) {
    this.contextProperties = contextProperties;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    InvocationContext context = InvocationContextHolder.deserialize(
        exchange.getRequest().getHeaders().getFirst(InvocationContextHolder.SERIALIZE_KEY));

    // copy external headers to context
    contextProperties.getHeaderContextMapper()
        .forEach((k, v) -> context.putContext(v, exchange.getRequest().getHeaders().getFirst(k)));
    contextProperties.getQueryContextMapper()
        .forEach((k, v) -> context.putContext(v, exchange.getRequest().getQueryParams().getFirst(k)));

    // copy or generate trace id
    if (context.getContext(InvocationContext.CONTEXT_TRACE_ID) == null) {
      context.putContext(InvocationContext.CONTEXT_TRACE_ID, InvocationContext.generateTraceId());
    }

    exchange.getAttributes().put(InvocationContextHolder.ATTRIBUTE_KEY, context);

    return chain.filter(exchange);
  }
}
