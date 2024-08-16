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

import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.adapters.RequestServiceInfoLoggerUtil;

import reactor.core.publisher.Mono;

public class RequestServiceInfoLoggerWebFilter implements OrderedWebFilter {
  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    return chain.filter(exchange).onErrorResume(Exception.class, (t) -> {
      InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
      RequestServiceInfoLoggerUtil.logServiceInfo(context, t);
      return Mono.error(t);
    });
  }

  @Override
  public int getOrder() {
    return GovernanceProperties.WEB_FILTER_REQUEST_LOGGER_ORDER;
  }
}
