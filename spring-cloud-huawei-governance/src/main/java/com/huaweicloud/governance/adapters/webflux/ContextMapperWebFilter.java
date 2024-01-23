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

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.processor.mapping.Mapper;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

public class ContextMapperWebFilter implements OrderedWebFilter {
  private final MapperHandler mapperHandler;

  public ContextMapperWebFilter(MapperHandler mapperHandler) {
    this.mapperHandler = mapperHandler;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    GovernanceRequestExtractor governanceRequest = WebFluxUtils.createProviderGovernanceRequest(exchange);
    Mapper mapper = mapperHandler.getActuator(governanceRequest);
    if (mapper == null || CollectionUtils.isEmpty(mapper.target())) {
      return chain.filter(exchange);
    }
    Map<String, String> properties = mapper.target();
    InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
    properties.forEach((k, v) -> {
      if (StringUtils.isEmpty(v)) {
        return;
      }
      if ("$U".equals(v)) {
        context.putContext(k, governanceRequest.apiPath());
      } else if ("$M".equals(v)) {
        context.putContext(k, governanceRequest.method());
      } else if (v.startsWith("$H{") && v.endsWith("}")) {
        context.putContext(k, governanceRequest.header(v.substring(3, v.length() - 1)));
      } else {
        context.putContext(k, v);
      }
    });
    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }
}
