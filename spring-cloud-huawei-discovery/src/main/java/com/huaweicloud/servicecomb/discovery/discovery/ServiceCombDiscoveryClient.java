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
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.apache.servicecomb.service.center.client.RegistrationEvents.HeartBeatEvent;
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
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.common.transport.DiscoveryBootstrapProperties;
import com.huaweicloud.servicecomb.discovery.client.model.DiscoveryConstants;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

public class ServiceCombDiscoveryClient implements DiscoveryClient, ApplicationEventPublisherAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombDiscoveryClient.class);

  private ServiceCenterClient serviceCenterClient;

  private DiscoveryBootstrapProperties discoveryProperties;

  private ServiceCenterDiscovery serviceCenterDiscovery;

  private ServiceCombRegistration serviceCombRegistration;

  private ApplicationEventPublisher applicationEventPublisher;

  private final AtomicLong changeId = new AtomicLong(0);

  public ServiceCombDiscoveryClient(DiscoveryBootstrapProperties discoveryProperties,
      ServiceCenterClient serviceCenterClient, ServiceCombRegistration serviceCombRegistration) {
    this.discoveryProperties = discoveryProperties;
    this.serviceCenterClient = serviceCenterClient;
    this.serviceCombRegistration = serviceCombRegistration;

    serviceCenterDiscovery = new ServiceCenterDiscovery(serviceCenterClient, EventManager.getEventBus());
    serviceCenterDiscovery.setPollInterval(discoveryProperties.getPollInterval());
    EventManager.getEventBus().register(this);
  }

  @Subscribe
  public void onHeartBeatEvent(HeartBeatEvent event) {
    if (event.isSuccess()) {
      serviceCenterDiscovery.updateMyselfServiceId(serviceCombRegistration.getMicroservice().getServiceId());
      // startDiscovery will check if already started, can call several times
      serviceCenterDiscovery.startDiscovery();
    }
  }

  // 适配 Spring Cloud HeartbeatEvent 事件。 当实例发生变更的时候，通过 HeartbeatEvent 通知
  // DiscoveryClient 拉取实例。
  @Subscribe
  public void onInstanceChangedEvent(InstanceChangedEvent event) {
    this.applicationEventPublisher.publishEvent(new HeartbeatEvent(this, changeId.getAndIncrement()));
  }

  @Override
  public String description() {
    return "SerivceComb Discovery";
  }

  /**
   * assert that app name and service name do not contain "."
   */
  private SubscriptionKey parseMicroserviceName(String serviceId) {
    int idxAt = serviceId.indexOf(DiscoveryConstants.APP_SERVICE_SEPRATOR);
    if (idxAt == -1) {
      return new SubscriptionKey(discoveryProperties.getAppName(), serviceId);
    }
    return new SubscriptionKey(serviceId.substring(0, idxAt), serviceId.substring(idxAt + 1));
  }

  @Override
  public List<ServiceInstance> getInstances(String serviceId) {
    SubscriptionKey subscriptionKey = parseMicroserviceName(serviceId);
    serviceCenterDiscovery.registerIfNotPresent(subscriptionKey);
    List<MicroserviceInstance> instances = serviceCenterDiscovery.getInstanceCache(subscriptionKey);

    if (instances == null) {
      return Collections.emptyList();
    }
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
        if (validMicroserviceName(microservice) != null) {
          serviceList.add(microservice.getServiceName());
        }
      }
      return serviceList;
    } catch (OperationException e) {
      LOGGER.error("getServices failed", e);
    }
    return serviceList;
  }

  private String validMicroserviceName(Microservice microservice) {
    if (microservice.getAppId().equals(DiscoveryConstants.DEFAULT_APPID) && microservice.getServiceName()
        .equals(DiscoveryConstants.SERVICE_CENTER)) {
      return null;
    }
    if (microservice.getAppId().equals(discoveryProperties.getAppName())) {
      return microservice.getServiceName();
    }
    if (Boolean.parseBoolean(microservice.getProperties().get(DiscoveryConstants.CONFIG_ALLOW_CROSS_APP_KEY))) {
      return microservice.getAppId() + DiscoveryConstants.APP_SERVICE_SEPRATOR + microservice.getServiceName();
    }
    return null;
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }
}
