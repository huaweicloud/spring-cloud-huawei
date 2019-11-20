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

package org.springframework.cloud.servicecomb.discovery.registry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.common.exception.ServiceCombException;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstanceSingleResponse;
import org.springframework.cloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import org.springframework.cloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

public class ServiceCombServiceRegistry implements ServiceRegistry<ServiceCombRegistration> {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServiceRegistry.class);

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private ServiceCombClient serviceCombClient;

  private HeartbeatScheduler heartbeatScheduler;

  private String serviceID = null;

  private String instanceID = null;

  public ServiceCombServiceRegistry(ServiceCombClient serviceCombClient, HeartbeatScheduler heartbeatScheduler,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    this.serviceCombClient = serviceCombClient;
    this.heartbeatScheduler = heartbeatScheduler;
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
  }

  @Override
  public void register(ServiceCombRegistration registration) {
    loopRegister(registration);
    LOGGER.info("register success,instanceID=" + instanceID + ";serviceID=" + serviceID);
    heartbeatScheduler.add(instanceID, serviceID);
  }

  private void loopRegister(ServiceCombRegistration registration) {
    Microservice microservice = RegistryHandler.buildMicroservice(registration);
    while (true) {
      try {
        serviceID = serviceCombClient.getServiceId(microservice);
        if (null == serviceID) {
          serviceID = serviceCombClient.registerMicroservice(microservice);
        }
        MicroserviceInstance microserviceInstance = RegistryHandler
            .buildMicroServiceInstances(serviceID, microservice, serviceCombDiscoveryProperties);
        instanceID = serviceCombClient.registerInstance(microserviceInstance);
        if (null != instanceID) {
          serviceCombClient.autoDiscovery(serviceCombDiscoveryProperties.isAutoDiscovery());
          break;
        }
      } catch (ServiceCombException e) {
        serviceCombClient.toggle();
        LOGGER.warn("register failed, will retry. please check config file. message=" + e.getMessage());
      }
      delay();
    }
  }

  private void delay() {
    try {
      Thread.sleep(ServiceRegistryConfig.DEFAULT_DELAY_TIME);
    } catch (InterruptedException e) {
      LOGGER.warn("thread interrupted.");
    }
  }

  @Override
  public void deregister(ServiceCombRegistration registration) {
    heartbeatScheduler.remove(instanceID);
    try {
      serviceCombClient.deRegisterInstance(serviceID, instanceID);
    } catch (ServiceCombException e) {
      LOGGER.error("deRegisterInstance failed", e);
    }
  }


  @Override
  public void close() {
    LOGGER.info("close");
  }

  @Override
  public void setStatus(ServiceCombRegistration registration, String status) {
    try {
      serviceCombClient.updateInstanceStatus(serviceID, instanceID, status);
    } catch (ServiceCombException e) {
      LOGGER.error("setStatus failed", e);
    }
  }

  @Override
  public String getStatus(ServiceCombRegistration registration) {
    try {
      MicroserviceInstanceSingleResponse instance = serviceCombClient.getInstance(serviceID, instanceID);
      if (instance != null && instance.getInstance() != null) {
        return instance.getInstance().getStatus().name();
      }
    } catch (ServiceCombException e) {
      LOGGER.error("getStatus failed", e);
    }
    return null;
  }


  public String getServiceID() {
    return serviceID;
  }

  public String getInstanceID() {
    return instanceID;
  }
}
