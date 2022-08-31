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
  private final ContextProperties contextProperties;

  private final AccessLogLogger accessLogLogger;

  public AccessLogGlobalFilter(ContextProperties contextProperties, AccessLogLogger accessLogLogger) {
    this.contextProperties = contextProperties;
    this.accessLogLogger = accessLogLogger;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    if (contextProperties.isEnableTraceInfo()) {
      Response<ServiceInstance> response = exchange.getAttribute(
          ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
      String service = "";
      if (response != null) {
        service = response.getServer().getServiceId() + ":" + response.getServer().getHost();
      }

      InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
      assert context != null;
      accessLogLogger.log("event=[{}],traceId=[{}],request=[{}],target=[{}]",
          "Gateway send request",
          context.getContext(InvocationContext.CONTEXT_TRACE_ID),
          exchange.getRequest().getURI(),
          service);
    }

    long begin = System.currentTimeMillis();
    return chain.filter(exchange).doOnSuccess(v -> {
      InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
      assert context != null;
      accessLogLogger.log("event=[{}],traceId=[{}],request=[{}],status=[{}],time=[{}]",
          "Gateway finish request",
          context.getContext(InvocationContext.CONTEXT_TRACE_ID),
          exchange.getRequest().getURI(),
          exchange.getResponse().getRawStatusCode(),
          System.currentTimeMillis() - begin);
    });
  }

  @Override
  public int getOrder() {
    // this filter executed after ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER
    return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 10;
  }
}
