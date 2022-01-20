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

package com.huaweicloud.servicecomb.discovery.discovery;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.http.client.event.ConfigCenterEndpointChangedEvent;
import org.apache.servicecomb.http.client.event.EventManager;
import org.apache.servicecomb.http.client.event.KieEndpointEndPointChangeEvent;
import org.apache.servicecomb.http.client.event.ServiceCenterEndpointChangeEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.servicecomb.discovery.client.model.DiscoveryConstants;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

public class IpPointManger {

  private boolean isInit = false;

  private ServiceCenterDiscovery serviceCenterDiscovery;

  private MicroserviceInstance microserviceInstance;

  public IpPointManger(ServiceCenterClient serviceCenterClient, ServiceCombRegistration serviceCombRegistration) {
    this.serviceCenterDiscovery = new ServiceCenterDiscovery(serviceCenterClient,
        com.huaweicloud.common.event.EventManager.getEventBus());
    this.microserviceInstance = serviceCombRegistration.getMicroserviceInstance();
    com.huaweicloud.common.event.EventManager.getEventBus().register(this);
  }

  @Subscribe
  public void onHeartBeatEvent(HeartBeatEvent event) {
    if (isInit) {
      return;
    }
    if (event.isSuccess()) {
      initAutoDiscovery();
    }
  }

  public void initAutoDiscovery() {
    isInit = true;
    InitSCEndPointNew();
    InitKieEndPointNew();
    InitCCEndPointNew();
  }

  private void InitSCEndPointNew() {
    List<MicroserviceInstance> instances = findServiceInstance(DiscoveryConstants.DEFAULT_APPID,
        DiscoveryConstants.SERVICE_CENTER, DiscoveryConstants.VERSION_RULE_LATEST);
    if (instances.size() <= 0) {
      isInit = false;
      return;
    }

    List<String> sameAvailableZone = new ArrayList<>();
    List<String> sameAvailableRegion = new ArrayList<>();
    for (MicroserviceInstance instance : instances) {
      if (regionAndAZMatch(this.microserviceInstance, instance)) {
        sameAvailableZone.addAll(instance.getEndpoints());
      } else {
        sameAvailableRegion.addAll(instance.getEndpoints());
      }
    }
    EventManager.post(new ServiceCenterEndpointChangeEvent(sameAvailableZone, sameAvailableRegion));
  }

  private void InitKieEndPointNew() {
    List<MicroserviceInstance> instances = findServiceInstance(DiscoveryConstants.DEFAULT_APPID,
        DiscoveryConstants.KIE_NAME, DiscoveryConstants.VERSION_RULE_LATEST);
    if (instances.size() <= 0) {
      return;
    }

    List<String> sameAvailableZone = new ArrayList<>();
    List<String> sameAvailableRegion = new ArrayList<>();
    for (MicroserviceInstance instance : instances) {
      if (regionAndAZMatch(this.microserviceInstance, instance)) {
        sameAvailableZone.addAll(instance.getEndpoints());
      } else {
        sameAvailableRegion.addAll(instance.getEndpoints());
      }
    }
    EventManager.post(new KieEndpointEndPointChangeEvent(sameAvailableZone, sameAvailableRegion));
  }

  private void InitCCEndPointNew() {
    List<MicroserviceInstance> instances = findServiceInstance(DiscoveryConstants.DEFAULT_APPID,
        DiscoveryConstants.CONFIG_CENTER_NAME, DiscoveryConstants.VERSION_RULE_LATEST);
    if (instances.size() <= 0) {
      return;
    }

    List<String> sameAvailableZone = new ArrayList<>();
    List<String> sameAvailableRegion = new ArrayList<>();
    for (MicroserviceInstance instance : instances) {
      if (regionAndAZMatch(this.microserviceInstance, instance)) {
        sameAvailableZone.addAll(instance.getEndpoints());
      } else {
        sameAvailableRegion.addAll(instance.getEndpoints());
      }
    }
    EventManager.post(new ConfigCenterEndpointChangedEvent(sameAvailableZone, sameAvailableRegion));
  }

  public List<MicroserviceInstance> findServiceInstance(String appId, String serviceName, String versionRule) {
    return serviceCenterDiscovery.findServiceInstance(appId, serviceName, versionRule);
  }

  private boolean regionAndAZMatch(MicroserviceInstance myself, MicroserviceInstance target) {
    if (myself.getDataCenterInfo() == null) {
      // when instance have no datacenter info, it will match all other datacenters
      return true;
    }
    if (target.getDataCenterInfo() != null) {
      return myself.getDataCenterInfo().getRegion().equals(target.getDataCenterInfo().getRegion()) &&
          myself.getDataCenterInfo().getAvailableZone().equals(target.getDataCenterInfo().getAvailableZone());
    }
    return false;
  }
}
