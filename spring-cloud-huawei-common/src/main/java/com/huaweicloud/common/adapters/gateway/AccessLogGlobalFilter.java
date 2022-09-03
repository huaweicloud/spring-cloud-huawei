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

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.access.AccessLogLogger;
import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

public class AccessLogGlobalFilter implements GlobalFilter, Ordered {

  public static final int ACCESS_LOG_ORDER = ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 30;

  private final ContextProperties contextProperties;

  private final AccessLogLogger accessLogLogger;

  public AccessLogGlobalFilter(ContextProperties contextProperties, AccessLogLogger accessLogLogger) {
    this.contextProperties = contextProperties;
    this.accessLogLogger = accessLogLogger;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (!contextProperties.isEnableTraceInfo()) {
      return chain.filter(exchange);
    }

    Response<ServiceInstance> response = exchange.getAttribute(
        ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
    String service = response == null ? "" : response.getServer().getServiceId() + ":" + response.getServer().getHost();
    InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
    assert context != null;
    String request = exchange.getRequest().getPath().value();
    String source = exchange.getRequest().getRemoteAddress().getHostString();
    accessLogLogger.log(context,
        "Gateway start request",
        request,
        source,
        service,
        0,
        0L);

    long begin = System.currentTimeMillis();
    return chain.filter(exchange).doOnSuccess(v -> {
      accessLogLogger.log(context,
          "Gateway finish request",
          request,
          source,
          service,
          exchange.getResponse().getRawStatusCode(),
          System.currentTimeMillis() - begin);
    }).doOnError(error -> {
      accessLogLogger.log(context,
          "Gateway finish request(" + error.getClass().getName() + ")",
          request,
          source,
          service,
          -1,
          System.currentTimeMillis() - begin);
    });
  }

  @Override
  public int getOrder() {
    // this filter executed after RetryGlobalFilter
    return ACCESS_LOG_ORDER;
  }
}
