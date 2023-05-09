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

import com.huaweicloud.governance.adapters.loadbalancer.ServiceInstanceFilter;
import com.huaweicloud.governance.authentication.MicroserviceInstanceService;
import com.huaweicloud.governance.authentication.discovery.ServiceRegistration;
import com.huaweicloud.governance.authentication.instance.CommonInstance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZoneAwareServiceInstanceFilter implements ServiceInstanceFilter {

  private ServiceRegistration registration;

  @Value("${spring.cloud.servicecomb.discovery.denyCrossZoneLoadBalancing:false}")
  private boolean denyCrossZoneLoadBalancing;

  @Autowired
  public void setServiceCombRegistration(ServiceRegistration registration) {
    this.registration = registration;
  }

  @Override
  public List<ServiceInstance> filter(ServiceInstanceListSupplier supplier, List<ServiceInstance> instances,
      Request<?> request) {
    CommonInstance mySelf = registration.getCommonInstance();
    return zoneAwareDiscoveryFilter(mySelf, instances);
  }

  @Override
  public int getOrder() {
    return -2;
  }

  private List<ServiceInstance> zoneAwareDiscoveryFilter(CommonInstance mySelf, List<ServiceInstance> instances) {
    List<ServiceInstance> regionAndAZMatchList = new ArrayList<>();
    List<ServiceInstance> regionMatchList = new ArrayList<>();
    instances.forEach(serviceInstance -> {
      MicroserviceInstanceService instance = (MicroserviceInstanceService) serviceInstance;
      if (regionAndAZMatch(mySelf, instance.getAvailableZone(), instance.getRegion())) {
        regionAndAZMatchList.add(serviceInstance);
      } else if (regionMatch(mySelf, instance.getRegion())) {
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

  private boolean regionAndAZMatch(CommonInstance myself, String availableZone, String region) {
    if (myself.getCenterData() != null) {
      return myself.getCenterData().getRegion().equals(region) &&
          myself.getCenterData().getAvailableZone().equals(availableZone);
    }
    return false;
  }

  private boolean regionMatch(CommonInstance myself, String region) {
    if (myself.getCenterData() != null) {
      return myself.getCenterData().getRegion().equals(region);
    }
    return false;
  }
}
