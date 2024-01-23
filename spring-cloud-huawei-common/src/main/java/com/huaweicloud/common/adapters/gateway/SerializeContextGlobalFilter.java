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

package com.huaweicloud.common.adapters.gateway;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

public class SerializeContextGlobalFilter implements GlobalFilter, Ordered {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    exchange.mutate().request(r -> r.header(InvocationContextHolder.SERIALIZE_KEY,
        InvocationContextHolder.serialize(exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY))));
    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    // LOWEST_PRECEDENCE are filters executed after response
    return Ordered.LOWEST_PRECEDENCE - 1;
  }
}
