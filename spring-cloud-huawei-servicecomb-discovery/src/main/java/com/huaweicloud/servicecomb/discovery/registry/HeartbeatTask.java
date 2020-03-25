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

import com.huaweicloud.common.cache.RegisterCache;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import com.huaweicloud.servicecomb.discovery.client.model.HeardBeatStatus;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.HeartbeatRequest;

/**
 * @Author wangqijun
 * @Date 11:13 2019-07-15
 **/

public class HeartbeatTask implements Runnable {
  private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatTask.class);

  private HeartbeatRequest heartbeatRequest;

  private ServiceCombClient serviceCombClient;

  private AtomicBoolean isInitialize = new AtomicBoolean(true);

  private ServiceCombSwaggerHandler serviceCombSwaggerHandler;

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private TagsProperties tagsProperties;

  private ServiceCombRegistration registration;

  public HeartbeatTask(HeartbeatRequest heartbeatRequest, ServiceCombClient serviceCombClient) {
    this.heartbeatRequest = heartbeatRequest;
    this.serviceCombClient = serviceCombClient;
  }

  public void setServiceCombSwaggerHandler(
      ServiceCombSwaggerHandler serviceCombSwaggerHandler) {
    this.serviceCombSwaggerHandler = serviceCombSwaggerHandler;
  }

  public void setServiceCombDiscoveryProperties(
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
  }

  public void setTagsProperties(
      TagsProperties tagsProperties) {
    this.tagsProperties = tagsProperties;
  }

  public void setRegistration(
      ServiceCombRegistration registration) {
    this.registration = registration;
  }

  @Override
  public void run() {
    try {
      HeardBeatStatus result = serviceCombClient.heartbeat(heartbeatRequest);
      if (result.equals(HeardBeatStatus.FAILED)) {
        retryRegister(registration);
      }
    } catch (ServiceCombException e) {
      LOGGER.warn("heartbeat failed.", e);
    }
  }

  private void retryRegister(ServiceCombRegistration registration) {
    LOGGER.info("retry registry to service center.");
    Microservice microservice = RegistryHandler.buildMicroservice(registration);
    if (serviceCombSwaggerHandler != null) {
      serviceCombSwaggerHandler.init(serviceCombDiscoveryProperties.getAppName(),
          serviceCombDiscoveryProperties.getServiceName());
      microservice.setSchemas(serviceCombSwaggerHandler.getSchemas());
    }
    try {
      String serviceID = serviceCombClient.getServiceId(microservice);
      if (null == serviceID) {
        serviceID = serviceCombClient.registerMicroservice(microservice);
      }
      if (serviceCombSwaggerHandler != null) {
        serviceCombSwaggerHandler.registerSwagger(serviceID, microservice.getSchemas());
      }
      MicroserviceInstance microserviceInstance = RegistryHandler
          .buildMicroServiceInstances(serviceID, microservice, serviceCombDiscoveryProperties,
              tagsProperties);
      String instanceID = serviceCombClient.registerInstance(microserviceInstance);
      if (null != instanceID) {
        serviceCombClient.autoDiscovery(serviceCombDiscoveryProperties.isAutoDiscovery());
        return;
      }
      RegisterCache.setInstanceID(instanceID);
      RegisterCache.setServiceID(serviceID);
      LOGGER.info("register success,instanceID=" + instanceID + ";serviceID=" + serviceID);
      isInitialize.compareAndSet(true, false);
    } catch (ServiceCombException e) {
      serviceCombClient.toggle();
      LOGGER.warn(
          "register failed, will retry. please check config file. message=" + e.getMessage());
    }
  }

}
