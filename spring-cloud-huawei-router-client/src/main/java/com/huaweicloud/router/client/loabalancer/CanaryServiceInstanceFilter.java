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

package com.huaweicloud.router.client.loabalancer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.common.adapters.loadbalancer.DecorateLoadBalancerRequest;
import com.huaweicloud.common.adapters.loadbalancer.ServiceInstanceFilter;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.util.HeaderUtil;
import com.huaweicloud.router.client.RouterConstant;

public class CanaryServiceInstanceFilter implements ServiceInstanceFilter {
  private final AbstractRouterDistributor<ServiceInstance, MicroserviceInstance> routerDistributor;

  private final RouterFilter routerFilter;

  @Autowired
  public CanaryServiceInstanceFilter(AbstractRouterDistributor<ServiceInstance, MicroserviceInstance> routerDistributor,
      RouterFilter routerFilter) {
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

    // headers from outer request
    InvocationContext invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    String headers = invocationContext.getContext(RouterConstant.CONTEXT_HEADER);
    if (headers != null) {
      canaryHeaders.putAll(HeaderUtil.deserialize(headers));
    }
    // headers from invocation context
    canaryHeaders.putAll(invocationContext.getContext());
    // headers from current request
    String targetServiceName = instances.get(0).getServiceId();
    DefaultRequestContext context = (DefaultRequestContext) request.getContext();
    Object clientRequest = context.getClientRequest();
    HttpHeaders httpHeaders;
    if (clientRequest instanceof DecorateLoadBalancerRequest) {
      // rest template
      httpHeaders = ((DecorateLoadBalancerRequest) clientRequest).getRequest().getHeaders();
    } else {
      // feign
      httpHeaders = ((RequestData) clientRequest).getHeaders();
    }
    canaryHeaders.putAll(httpHeaders.toSingleValueMap());

    return routerFilter
        .getFilteredListOfServers(instances, targetServiceName, canaryHeaders,
            routerDistributor);
  }

  @Override
  public int getOrder() {
    return -1;
  }
}