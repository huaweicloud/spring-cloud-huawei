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

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import com.huaweicloud.common.cache.RegisterCache;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstanceSingleResponse;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

public class ServiceCombServiceRegistry implements ServiceRegistry<ServiceCombRegistration> {

  @Autowired(required = false)
  private ServiceCombSwaggerHandler serviceCombSwaggerHandler;

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServiceRegistry.class);

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private ServiceCombClient serviceCombClient;

  private HeartbeatScheduler heartbeatScheduler;

  private TagsProperties tagsProperties;

  private String serviceID = null;

  private String instanceID = null;

  public ServiceCombServiceRegistry(ServiceCombClient serviceCombClient,
      HeartbeatScheduler heartbeatScheduler,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      TagsProperties tagsProperties) {
    this.tagsProperties = tagsProperties;
    this.serviceCombClient = serviceCombClient;
    this.heartbeatScheduler = heartbeatScheduler;
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
  }

  @Override
  public void register(ServiceCombRegistration registration) {
    loopRegister(registration);
    RegisterCache.setInstanceID(instanceID);
    RegisterCache.setServiceID(serviceID);
    LOGGER.info("register success,instanceID=" + instanceID + ";serviceID=" + serviceID);
    heartbeatScheduler.add(registration, serviceCombSwaggerHandler);
  }

  private void loopRegister(ServiceCombRegistration registration) {
    Microservice microservice = RegistryHandler.buildMicroservice(registration);
    while (true) {
      try {
        serviceID = serviceCombClient.getServiceId(microservice);
        if (null == serviceID) {
          serviceID = serviceCombClient.registerMicroservice(microservice);
        }
        if (serviceCombSwaggerHandler != null) {
          serviceCombSwaggerHandler.initAndRegister(serviceCombDiscoveryProperties.getAppName(),
              serviceCombDiscoveryProperties.getServiceName(), serviceID);
        }
        MicroserviceInstance microserviceInstance = RegistryHandler
            .buildMicroServiceInstances(serviceID, microservice, serviceCombDiscoveryProperties,
                tagsProperties);
        instanceID = serviceCombClient.registerInstance(microserviceInstance);
        if (null != instanceID) {
          serviceCombClient.autoDiscovery(serviceCombDiscoveryProperties.isAutoDiscovery());
          break;
        }
      } catch (ServiceCombException e) {
        serviceCombClient.toggle();
        LOGGER.warn(
            "register failed, will retry. please check config file. message=" + e.getMessage());
      }
    }
  }

  @Override
  public void deregister(ServiceCombRegistration registration) {
    heartbeatScheduler.remove();
    try {
      serviceCombClient
          .deRegisterInstance(RegisterCache.getServiceID(), RegisterCache.getInstanceID());
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
      MicroserviceInstanceSingleResponse instance = serviceCombClient
          .getInstance(serviceID, instanceID);
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
