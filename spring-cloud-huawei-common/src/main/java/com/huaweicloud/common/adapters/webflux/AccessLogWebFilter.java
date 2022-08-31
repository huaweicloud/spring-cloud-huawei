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

import com.huaweicloud.common.access.AccessLogLogger;
import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

public class AccessLogWebFilter implements OrderedWebFilter {
  private final ContextProperties contextProperties;

  private final AccessLogLogger accessLogLogger;

  public AccessLogWebFilter(ContextProperties contextProperties, AccessLogLogger accessLogLogger) {
    this.contextProperties = contextProperties;
    this.accessLogLogger = accessLogLogger;
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (contextProperties.isEnableTraceInfo()) {
      InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
      accessLogLogger.log("event=[{}],traceId=[{}],request=[{}]",
          "WebFlux receive request",
          context.getContext(InvocationContext.CONTEXT_TRACE_ID),
          exchange.getRequest().getURI());
    }

    long begin = System.currentTimeMillis();
    return chain.filter(exchange).doOnSuccess(v -> {
      if (contextProperties.isEnableTraceInfo()) {
        InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
        accessLogLogger.log("event=[{}],traceId=[{}],request=[{}],status=[{}],time=[{}]",
            "WebFlux finish request",
            context.getContext(InvocationContext.CONTEXT_TRACE_ID),
            exchange.getRequest().getURI(),
            exchange.getResponse().getRawStatusCode(),
            System.currentTimeMillis() - begin);
      }
    });
  }
}
