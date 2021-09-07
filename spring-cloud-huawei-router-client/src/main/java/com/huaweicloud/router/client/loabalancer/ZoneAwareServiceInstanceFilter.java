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

import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZoneAwareServiceInstanceFilter implements ServiceInstanceFilter {

  @Autowired
  private ServiceCombRegistration serviceCombRegistration;

  @Value("spring.cloud.servicecomb.discovery.denyCrossZoneLoadBalancing:false")
  private boolean denyCrossZoneLoadBalancing;

  @Override
  public List<ServiceInstance> filter(ServiceInstanceListSupplier supplier, List<ServiceInstance> instances,
      Request<?> request) {
    MicroserviceInstance mySelf = serviceCombRegistration.getMicroserviceInstance();
    List<ServiceInstance> filterInstances = zoneAwareDiscoveryFilter(mySelf, instances);
    return filterInstances;
  }

  @Override
  public int getOrder() {
    return -2;
  }

  private List<ServiceInstance> zoneAwareDiscoveryFilter(MicroserviceInstance mySelf, List<ServiceInstance> instances) {
    List<ServiceInstance> regionAndAZMatchList = new ArrayList<>();
    List<ServiceInstance> regionMatchList = new ArrayList<>();
    instances.forEach(serviceInstance -> {
      ServiceCombServiceInstance instance = (ServiceCombServiceInstance) serviceInstance;
      if (regionAndAZMatch(mySelf, instance.getMicroserviceInstance())) {
        regionAndAZMatchList.add(serviceInstance);
      } else if (regionMatch(mySelf, instance.getMicroserviceInstance())) {
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

  private boolean regionAndAZMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself.getDataCenterInfo() != null && target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion()) &&
          myself.getDataCenterInfo().getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }

  private boolean regionMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion());
    }
    return false;
  }
}
