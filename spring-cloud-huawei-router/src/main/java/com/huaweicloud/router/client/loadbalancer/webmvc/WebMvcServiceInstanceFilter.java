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

package com.huaweicloud.router.client.loadbalancer.webmvc;

import java.util.List;

import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;

import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.adapters.loadbalancer.ServiceInstanceFilter;
import com.huaweicloud.router.client.loadbalancer.RouterServiceInstanceFilter;

public class WebMvcServiceInstanceFilter implements ServiceInstanceFilter {
  private final AbstractRouterDistributor<ServiceInstance> routerDistributor;

  private final RouterFilter routerFilter;

  private final Environment env;

  @Autowired
  public WebMvcServiceInstanceFilter(AbstractRouterDistributor<ServiceInstance> routerDistributor,
      RouterFilter routerFilter, Environment env) {
    this.routerDistributor = routerDistributor;
    this.routerFilter = routerFilter;
    this.env = env;
  }

  @Override
  public List<ServiceInstance> filter(ServiceInstanceListSupplier supplier, List<ServiceInstance> instances,
      Request<?> request) {
    return RouterServiceInstanceFilter.filterInstance(InvocationContextHolder.getOrCreateInvocationContext(), instances,
        request, routerFilter, routerDistributor);
  }

  @Override
  public int getOrder() {
    return env.getProperty("spring.cloud.loadbalance.filter.router.order", int.class, -100);
  }
}
