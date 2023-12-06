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
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.governance.adapters.loadbalancer.DecorateLoadBalancerRequest;

public class RouterServiceInstanceFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RouterServiceInstanceFilter.class);

  public static List<ServiceInstance> filterInstance(InvocationContext invocationContext, List<ServiceInstance> instances,
      Request<?> request, RouterFilter routerFilter, AbstractRouterDistributor<ServiceInstance> routerDistributor) {
    if (CollectionUtils.isEmpty(instances)) {
      return instances;
    }
    Map<String, String> canaryHeaders;
    if (invocationContext == null) {
      canaryHeaders = new HashMap<>();
    } else {
      // headers from invocation context
      canaryHeaders = new HashMap<>(invocationContext.getContext());
    }
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
        .getFilteredListOfServers(instances, targetServiceName, canaryHeaders, routerDistributor);
  }
}
