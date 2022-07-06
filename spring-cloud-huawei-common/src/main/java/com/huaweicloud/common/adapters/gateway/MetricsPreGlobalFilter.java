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

import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.metrics.InvocationMetrics;

public class MetricsPreGlobalFilter implements PreGlobalFilter {
  @Override
  public void process(ServerWebExchange exchange) {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    context.putLocalContext(InvocationMetrics.CONTEXT_TIME, System.currentTimeMillis());
    context.putLocalContext(InvocationMetrics.CONTEXT_OPERATION, buildOperation(exchange.getRequest()));
  }

  private String buildOperation(ServerHttpRequest request) {
    StringBuilder sb = new StringBuilder();
    sb.append(request.getMethod());
    sb.append(" ");
    sb.append(request.getURI().getPath());
    return sb.toString();
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }
}
