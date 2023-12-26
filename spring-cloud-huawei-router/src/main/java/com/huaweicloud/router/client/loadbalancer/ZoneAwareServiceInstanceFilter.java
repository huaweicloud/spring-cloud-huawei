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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;

import com.huaweicloud.governance.adapters.loadbalancer.ServiceInstanceFilter;

public class ZoneAwareServiceInstanceFilter implements ServiceInstanceFilter {

  private final Registration registration;

  private final ZoneAwareFilterAdapter adapter;

  @Value("${spring.cloud.servicecomb.discovery.denyCrossZoneLoadBalancing:false}")
  private boolean denyCrossZoneLoadBalancing;

  private final Environment env;

  public ZoneAwareServiceInstanceFilter(Registration registration, ZoneAwareFilterAdapter adapter,
      Environment environment) {
    this.registration = registration;
    this.adapter = adapter;
    this.env = environment;
  }

  @Override
  public List<ServiceInstance> filter(ServiceInstanceListSupplier supplier, List<ServiceInstance> instances,
      Request<?> request) {
    return zoneAwareDiscoveryFilter(instances);
  }

  @Override
  public int getOrder() {
    return env.getProperty("spring.cloud.loadbalance.filter.zone-aware.order", int.class, -200);
  }

  private List<ServiceInstance> zoneAwareDiscoveryFilter(List<ServiceInstance> instances) {
    List<ServiceInstance> regionAndAZMatchList = new ArrayList<>();
    List<ServiceInstance> regionMatchList = new ArrayList<>();
    instances.forEach(serviceInstance -> {
      if (regionAndAZMatch(this.adapter.getAvailableZone(serviceInstance), this.adapter.getRegion(serviceInstance))) {
        regionAndAZMatchList.add(serviceInstance);
      } else if (regionMatch(this.adapter.getRegion(serviceInstance))) {
        regionMatchList.add(serviceInstance);
      }
    });
    if (!regionAndAZMatchList.isEmpty()) {
      return regionAndAZMatchList;
    }
    if (!regionMatchList.isEmpty()) {
      return regionMatchList;
    }
    if (!denyCrossZoneLoadBalancing) {
      return instances;
    } else {
      return Collections.emptyList();
    }
  }

  private boolean regionAndAZMatch(String availableZone, String region) {
    if (adapter.getRegion(registration) != null && adapter.getAvailableZone(registration) != null) {
      return adapter.getRegion(registration).equals(region) &&
          adapter.getAvailableZone(registration).equals(availableZone);
    }
    return false;
  }

  private boolean regionMatch(String region) {
    if (adapter.getRegion(registration) != null) {
      return adapter.getRegion(registration).equals(region);
    }
    return false;
  }
}
