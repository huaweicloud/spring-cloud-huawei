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

import java.util.List;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

public class DecorateGlobalFilter implements GlobalFilter, Ordered {
  private final List<PreGlobalFilter> preGlobalFilters;

  private final List<PostGlobalFilter> postGlobalFilters;

  public DecorateGlobalFilter(List<PreGlobalFilter> preGlobalFilters, List<PostGlobalFilter> postGlobalFilters) {
    this.preGlobalFilters = preGlobalFilters;
    this.postGlobalFilters = postGlobalFilters;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    preProcess(exchange);
    return chain.filter(exchange).doOnSuccess(v -> postProcess(exchange))
        .doOnError(t -> postProcess(exchange));
  }

  private void preProcess(ServerWebExchange exchange) {
    if (preGlobalFilters != null) {
      preGlobalFilters.forEach(filter -> filter.process(exchange));
    }
  }

  private void postProcess(ServerWebExchange exchange) {
    if (postGlobalFilters != null) {
      postGlobalFilters.forEach(filter -> filter.process(exchange));
    }
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
