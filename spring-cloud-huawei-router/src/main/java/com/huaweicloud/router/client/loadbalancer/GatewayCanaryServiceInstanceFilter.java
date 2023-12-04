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

package com.huaweicloud.router.client.loadbalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.adapters.loadbalancer.DecorateLoadBalancerRequest;
import com.huaweicloud.governance.adapters.loadbalancer.ServiceInstanceFilter;

import reactor.core.publisher.Mono;

public class GatewayCanaryServiceInstanceFilter implements ServiceInstanceFilter, WebFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(GatewayCanaryServiceInstanceFilter.class);

  private final AbstractRouterDistributor<ServiceInstance> routerDistributor;

  private final RouterFilter routerFilter;

  private InvocationContext invocationContext;

  @Autowired
  public GatewayCanaryServiceInstanceFilter(
      AbstractRouterDistributor<ServiceInstance> routerDistributor, RouterFilter routerFilter) {
    this.routerDistributor = routerDistributor;
    this.routerFilter = routerFilter;
  }

  @Override
  public List<ServiceInstance> filter(ServiceInstanceListSupplier supplier, List<ServiceInstance> instances,
      Request<?> request) {
    if (CollectionUtils.isEmpty(instances)) {
      return instances;
    }

    Map<String, String> canaryHeaders = new HashMap<>();

    // headers from invocation context
    canaryHeaders.putAll(invocationContext.getContext());

    // headers from current request
    String targetServiceName = instances.get(0).getServiceId();
    Object context = request.getContext();
    if (context instanceof DefaultRequestContext) {
      Object clientRequest = ((DefaultRequestContext) context).getClientRequest();
      HttpHeaders httpHeaders;
      if (clientRequest instanceof DecorateLoadBalancerRequest) {
        // rest template
        httpHeaders = ((DecorateLoadBalancerRequest) clientRequest).getRequest().getHeaders();
      } else if (clientRequest instanceof RequestData) {
        // feign
        httpHeaders = ((RequestData) clientRequest).getHeaders();
      } else {
        LOGGER.warn("not implemented client request {}.", clientRequest == null ? null : clientRequest.getClass());
        httpHeaders = HttpHeaders.EMPTY;
      }
      canaryHeaders.putAll(httpHeaders.toSingleValueMap());
    } else {
      LOGGER.warn("not implemented context {}.", context == null ? null : context.getClass());
    }

    return routerFilter
        .getFilteredListOfServers(instances, targetServiceName, canaryHeaders,
            routerDistributor);
  }

  @Override
  public int getOrder() {
    return CANARY_ORDER;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    invocationContext = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
    return chain.filter(exchange);
  }
}
