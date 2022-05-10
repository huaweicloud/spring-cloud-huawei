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

import com.huaweicloud.router.client.track.RouterTrackContext;

import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.http.HttpHeaders;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CanaryServiceInstanceFilter implements ServiceInstanceFilter {

  private static final Logger LOGGER = LoggerFactory.getLogger(CanaryServiceInstanceFilter.class);

  private final AbstractRouterDistributor<ServiceInstance, MicroserviceInstance> routerDistributor;

  private final RouterFilter routerFilter;

  @Autowired
  public CanaryServiceInstanceFilter(AbstractRouterDistributor<ServiceInstance, MicroserviceInstance> routerDistributor, RouterFilter routerFilter) {
    this.routerDistributor = routerDistributor;
    this.routerFilter = routerFilter;
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public List<ServiceInstance> filter(ServiceInstanceListSupplier supplier, List<ServiceInstance> instances,
      Request<?> request) {
    String  targetServiceName = ((ServiceCombServiceInstance) instances.get(0)).getServiceId();
    DefaultRequestContext context = (DefaultRequestContext) request.getContext();
    Object clientRequest = context.getClientRequest();
    HttpHeaders httpHeaders;
    if (clientRequest instanceof RouterLoadBalancerRequest) {
      // rest template
      httpHeaders = ((RouterLoadBalancerRequest) clientRequest).getRequest().getHeaders();
    } else {
      // feign
      httpHeaders = ((RequestData) clientRequest).getHeaders();
    }

    Map<String, String> canaryHeaders = new HashMap<>();
    try {
      if (httpHeaders.getFirst(RouterTrackContext.ROUTER_TRACK_HEADER) != null) {
        canaryHeaders.putAll(
            JsonUtils.readValue(httpHeaders.getFirst(RouterTrackContext.ROUTER_TRACK_HEADER).getBytes(), Map.class));
      }
      canaryHeaders.putAll(httpHeaders.toSingleValueMap());
    } catch (IOException e) {
      LOGGER.warn("decode headers failed for {}", e.getMessage());
    }
    return routerFilter
        .getFilteredListOfServers(instances, targetServiceName, canaryHeaders,
            routerDistributor);
  }

  @Override
  public int getOrder() {
    return -1;
  }
}
