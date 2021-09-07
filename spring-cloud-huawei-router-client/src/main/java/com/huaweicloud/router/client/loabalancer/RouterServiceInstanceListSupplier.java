/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.Ordered;

import reactor.core.publisher.Flux;

@SuppressWarnings({"rawtype", "unchecked"})
public class RouterServiceInstanceListSupplier implements ServiceInstanceListSupplier {

  @Autowired
  private List<ServiceInstanceFilter> filters;

  private ServiceInstanceListSupplier delegate;

  public RouterServiceInstanceListSupplier(ServiceInstanceListSupplier delegate) {
    this.delegate = delegate;
  }

  @PostConstruct
  private void init() {
    if (filters != null) {
      Collections.sort(filters, Comparator.comparingInt(Ordered::getOrder));
    }
  }

  @Override
  public String getServiceId() {
    return this.delegate.getServiceId();
  }

  @Override
  @SuppressWarnings({"rawtypes", "unchecked"})
  public Flux<List<ServiceInstance>> get(Request request) {
    Flux<List<ServiceInstance>> result = delegate.get(request);
    return result.map(instances -> filter(instances, request));
  }

  @Override
  public Flux<List<ServiceInstance>> get() {
    return this.delegate.get();
  }

  @SuppressWarnings({"all"})
  private List<ServiceInstance> filter(List<ServiceInstance> instances, @SuppressWarnings({"all"}) Request<?> request) {
    if (filters == null) {
      return instances;
    }

    List<ServiceInstance> filteredInstances = instances;
    for (ServiceInstanceFilter instanceFilter : filters) {
      filteredInstances = instanceFilter.filter(this, filteredInstances, request);
    }
    return filteredInstances;
  }
}
