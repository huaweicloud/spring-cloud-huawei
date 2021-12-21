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

package com.huaweicloud.servicecomb.discovery.registry;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.ServiceCenterRegistration;
import org.apache.servicecomb.service.center.client.ServiceCenterWatch;
import org.apache.servicecomb.service.center.client.exception.OperationException;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.SchemaInfo;
import org.apache.servicecomb.service.center.client.model.ServiceCenterConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import com.huaweicloud.common.transport.DiscoveryBootstrapProperties;
import com.huaweicloud.servicecomb.discovery.client.model.DiscoveryConstants;

public class ServiceCombServiceRegistry implements ServiceRegistry<ServiceCombRegistration>, ApplicationContextAware {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServiceRegistry.class);

  // use bean name to avoid cyclic dependencies for swagger
  private static final String SWAGGER_BEAN_NAME = "serviceCombSwaggerHandler";

  private final DiscoveryBootstrapProperties discoveryBootstrapProperties;

  private final ServiceCenterClient serviceCenterClient;

  private ServiceCenterRegistration serviceCenterRegistration;

  private final ServiceCenterWatch watch;

  private ServiceCombRegistration serviceCombRegistration;

  private final ServiceCenterConfiguration serviceCenterConfiguration;

  private ApplicationContext applicationContext;

  public ServiceCombServiceRegistry(DiscoveryBootstrapProperties discoveryBootstrapProperties,
      ServiceCenterClient serviceCenterClient, @Autowired(required = false) ServiceCenterWatch watch) {
    this.serviceCenterClient = serviceCenterClient;
    this.watch = watch;
    this.discoveryBootstrapProperties = discoveryBootstrapProperties;
    this.serviceCenterConfiguration = new ServiceCenterConfiguration().setIgnoreSwaggerDifferent(
        discoveryBootstrapProperties.isIgnoreSwaggerDifferent());
  }

  @Subscribe
  public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
    if (event.isSuccess() && discoveryBootstrapProperties.isWatch()) {
      watch.startWatch(DiscoveryConstants.DEFAULT_PROJECT, serviceCombRegistration.getMicroservice().getServiceId());
    }
  }

  @Override
  public void register(ServiceCombRegistration registration) {
    serviceCombRegistration = registration;
    serviceCenterRegistration = new ServiceCenterRegistration(serviceCenterClient, serviceCenterConfiguration,
        EventManager.getEventBus());
    EventManager.getEventBus().register(this);
    serviceCenterRegistration.setMicroservice(registration.getMicroservice());
    serviceCenterRegistration.setMicroserviceInstance(registration.getMicroserviceInstance());
    serviceCenterRegistration.setHeartBeatInterval(discoveryBootstrapProperties.getHealthCheckInterval());
    serviceCenterRegistration.setHeartBeatRequestTimeout(discoveryBootstrapProperties.getHealthCheckRequestTimeout());

    addSchemaInfo(registration);

    serviceCenterRegistration.startRegistration();
  }

  private void addSchemaInfo(ServiceCombRegistration registration) {

    if (this.applicationContext.containsBean(SWAGGER_BEAN_NAME)) {
      ServiceCombSwaggerHandler serviceCombSwaggerHandler = this.applicationContext
          .getBean(SWAGGER_BEAN_NAME, ServiceCombSwaggerHandler.class);
      serviceCombSwaggerHandler
          .init(registration.getMicroservice().getAppId(), registration.getMicroservice().getServiceName());
      registration.getMicroservice().setSchemas(serviceCombSwaggerHandler.getSchemaIds());
      Map<String, String> contents = serviceCombSwaggerHandler.getSchemasMap();
      Map<String, String> summary = serviceCombSwaggerHandler.getSchemasSummaryMap();

      List<SchemaInfo> schemaInfos = serviceCombSwaggerHandler.getSchemaIds().stream()
          .map(id -> new SchemaInfo(id, contents.get(id), summary.get(id)))
          .collect(Collectors.toList());
      serviceCenterRegistration.setSchemaInfos(schemaInfos);
    }
  }

  @Override
  public void deregister(ServiceCombRegistration registration) {
    if (serviceCenterRegistration != null) {
      serviceCenterRegistration.stop();
      if (!StringUtils.isEmpty(registration.getMicroserviceInstance().getInstanceId())) {
        try {
          serviceCenterClient.deleteMicroserviceInstance(registration.getMicroserviceInstance().getServiceId(),
              registration.getMicroserviceInstance().getInstanceId());
        } catch (Exception e) {
          LOGGER.error("delete microservice failed. ", e);
        }
      }
    }
    if (watch != null) {
      watch.stop();
    }
  }

  @Override
  public void close() {
    LOGGER.info("service registry closed.");
  }

  @Override
  public void setStatus(ServiceCombRegistration registration, String status) {
    try {
      serviceCenterClient.updateMicroserviceInstanceStatus(registration.getMicroserviceInstance().getServiceId(),
          registration.getMicroserviceInstance().getInstanceId(), MicroserviceInstanceStatus.valueOf(status));
    } catch (OperationException e) {
      LOGGER.error("setStatus failed", e);
    }
  }

  @Override
  @SuppressWarnings("unchecked")
  public <T> T getStatus(ServiceCombRegistration registration) {
    try {
      MicroserviceInstance instance = serviceCenterClient
          .getMicroserviceInstance(registration.getMicroserviceInstance().getServiceId(),
              registration.getMicroserviceInstance().getInstanceId());
      return (T) (instance.getStatus() != null ? instance.getStatus().name() : null);
    } catch (OperationException e) {
      LOGGER.error("getStatus failed", e);
    }
    return null;
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
