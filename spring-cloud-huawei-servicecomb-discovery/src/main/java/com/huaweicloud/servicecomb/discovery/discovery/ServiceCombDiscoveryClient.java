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
import java.util.stream.Collectors;

import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery;
import org.apache.servicecomb.service.center.client.ServiceCenterDiscovery.SubscriptionKey;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroservicesResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.servicecomb.discovery.client.model.DiscoveryConstants;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;

public class ServiceCombDiscoveryClient implements DiscoveryClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombDiscoveryClient.class);

  private ServiceCenterClient serviceCenterClient;

  private ServiceCombDiscoveryProperties discoveryProperties;

  ServiceCenterDiscovery serviceCenterDiscovery;

  public ServiceCombDiscoveryClient(ServiceCombDiscoveryProperties discoveryProperties,
      ServiceCenterClient serviceCenterClient) {
    this.discoveryProperties = discoveryProperties;
    this.serviceCenterClient = serviceCenterClient;

    serviceCenterDiscovery = new ServiceCenterDiscovery(serviceCenterClient,
        EventManager.getEventBus());
    serviceCenterDiscovery.startDiscovery();
  }

  @Override
  public String description() {
    return "SerivceComb Discovery";
  }

  @Override
  public List<ServiceInstance> getInstances(String serviceId) {
    SubscriptionKey subscriptionKey = new SubscriptionKey(discoveryProperties.getAppName(), serviceId);
    if (!serviceCenterDiscovery.isRegistered(subscriptionKey)) {
      serviceCenterDiscovery.register(subscriptionKey);
    }
    List<MicroserviceInstance> instances = serviceCenterDiscovery.getInstanceCache(subscriptionKey);

    return instances.stream().map(item -> new ServiceCombServiceInstance(item)).collect(Collectors.toList());
  }

  @Override
  public List<String> getServices() {
    List<String> serviceList = new ArrayList<>();
    try {
      MicroservicesResponse microServiceResponse = serviceCenterClient.getMicroserviceList();
      if (microServiceResponse == null || microServiceResponse.getServices() == null) {
        return serviceList;
      }
      for (Microservice microservice : microServiceResponse.getServices()) {
        if (isAllowedMicroservice(microservice)) {
          serviceList.add(microservice.getServiceName());
        }
      }
      return serviceList;
    } catch (OperationException e) {
      LOGGER.error("getServices failed", e);
    }
    return serviceList;
  }

  private boolean isAllowedMicroservice(Microservice microservice) {
    if (microservice.getAppId().equals(discoveryProperties.getAppName()) ||
        Boolean.parseBoolean(microservice.getProperties().get(DiscoveryConstants.CONFIG_ALLOW_CROSS_APP_KEY))) {
      return true;
    }
    return false;
  }
}
