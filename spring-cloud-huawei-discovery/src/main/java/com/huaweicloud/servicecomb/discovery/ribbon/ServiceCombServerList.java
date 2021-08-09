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

package com.huaweicloud.servicecomb.discovery.ribbon;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServer;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.Server;

public class ServiceCombServerList extends AbstractServerList<Server> {
  private String serviceId;

  private DiscoveryClient discoveryClient;

  private ServiceCombRegistration serviceCombRegistration;

  public ServiceCombServerList(DiscoveryClient discoveryClient, ServiceCombRegistration serviceCombRegistration) {
    this.discoveryClient = discoveryClient;
    this.serviceCombRegistration = serviceCombRegistration;
  }

  @Override
  public void initWithNiwsConfig(IClientConfig iClientConfig) {
    this.serviceId = iClientConfig.getClientName();
  }

  @Override
  public List<Server> getInitialListOfServers() {
    return Collections.emptyList();
  }

  @Override
  public List<Server> getUpdatedListOfServers() {
    MicroserviceInstance mySelf = serviceCombRegistration.getMicroserviceInstance();
    List<ServiceInstance> instances = discoveryClient.getInstances(this.serviceId);
    if (serviceCombRegistration.getDiscoveryBootstrapProperties().isEnableZoneAware()) {
      instances = zoneAwareDiscoveryFilter(mySelf, instances);
    }
    return transform(instances);
  }

  private List<Server> transform(List<ServiceInstance> instanceList) {
    List<Server> serverList = new ArrayList<>();
    instanceList.forEach(
        instance -> serverList.add(new ServiceCombServer((ServiceCombServiceInstance) instance)));
    return serverList;
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
    return instances;
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
