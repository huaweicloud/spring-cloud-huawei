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

package com.huaweicloud.router.client.gateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.util.HeaderUtil;
import com.huaweicloud.router.client.RouterConstant;

import reactor.core.publisher.Mono;

public class RouterContextGlobalFilter implements GlobalFilter, Ordered {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
    if (context.getContext(RouterConstant.CONTEXT_HEADER) == null) {
      Map<String, String> routerContext = new HashMap<>();
      HttpHeaders headers = exchange.getRequest().getHeaders();
      for (String header : headers.keySet()) {
        routerContext.put(header, headers.getFirst(header));
      }
      context.putContext(RouterConstant.CONTEXT_HEADER, HeaderUtil.serialize(routerContext));
    }
    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    // this filter executed after ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER
    return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 30;
  }
}
