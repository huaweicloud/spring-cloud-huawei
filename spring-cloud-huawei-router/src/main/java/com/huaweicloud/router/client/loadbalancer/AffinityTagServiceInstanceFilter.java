/*
 * Copyright (C) 2023-2023 Huawei Technologies Co., Ltd. All rights reserved.
 *
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

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.governance.adapters.loadbalancer.ServiceInstanceFilter;

/**
 * affinity tag instance filter
 * @author provenceee
 * @since 2023-09-19
 */
public class AffinityTagServiceInstanceFilter implements ServiceInstanceFilter {
  private final Registration registration;

  private final AffinityTagFilterAdapter adapter;

  private final Environment env;

  public AffinityTagServiceInstanceFilter(Registration registration, AffinityTagFilterAdapter adapter,
      Environment environment) {
    this.registration = registration;
    this.adapter = adapter;
    this.env = environment;
  }

  @Override
  public List<ServiceInstance> filter(ServiceInstanceListSupplier supplier, List<ServiceInstance> instances,
      Request<?> request) {
    String affinityTag = adapter.getAffinityTag(registration);
    List<ServiceInstance> serviceInstances = instances.stream().filter(
        instance -> Objects.equals(affinityTag, adapter.getAffinityTag(instance))).collect(Collectors.toList());
    return CollectionUtils.isEmpty(serviceInstances) ? instances : serviceInstances;
  }

  @Override
  public int getOrder() {
    return env.getProperty("spring.cloud.loadbalance.filter.affinity-tag.order", int.class, 100);
  }
}
