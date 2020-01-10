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

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.common.util.NetUtil;
import com.huaweicloud.servicecomb.discovery.client.model.Framework;
import com.huaweicloud.servicecomb.discovery.client.model.HealthCheck;
import com.huaweicloud.servicecomb.discovery.client.model.HealthCheckMode;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceStatus;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

/**
 * @Author wangqijun
 * @Date 15:18 2019-08-08
 **/
public class RegistryHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryHandler.class);


  public static MicroserviceInstance buildMicroServiceInstances(String serviceID,
      Microservice microservice,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      TagsProperties tagsProperties) {
    MicroserviceInstance microserviceInstance = buildInstance(serviceID,
        serviceCombDiscoveryProperties, tagsProperties);
    List<MicroserviceInstance> instances = new ArrayList<>();
    instances.add(microserviceInstance);
    microservice.setInstances(instances);
    microservice.setStatus(MicroserviceStatus.UP);
    return microserviceInstance;
  }

  private static MicroserviceInstance buildInstance(String serviceID,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      TagsProperties tagsProperties) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setServiceId(serviceID);
    microserviceInstance.setHostName(NetUtil.getLocalHost());
    List<String> endPoints = new ArrayList<>();
    endPoints.add("rest://" + serviceCombDiscoveryProperties.getAddress() + ":"
        + serviceCombDiscoveryProperties.getPort());
    microserviceInstance.setEndpoints(endPoints);
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.PLATFORM);
    healthCheck.setInterval(serviceCombDiscoveryProperties.getHealthCheckInterval());
    healthCheck.setTimes(3);
    microserviceInstance.setHealthCheck(healthCheck);
    String currTime = String.valueOf(System.currentTimeMillis());
    microserviceInstance.setTimestamp(currTime);
    microserviceInstance.setModTimestamp(currTime);
    microserviceInstance.setVersion(serviceCombDiscoveryProperties.getVersion());
    microserviceInstance.setProperties(tagsProperties.getTag());
    return microserviceInstance;
  }

  public static Microservice buildMicroservice(ServiceCombRegistration registration) {
    Microservice microservice = new Microservice();
    microservice.setAppId(registration.getAppName());
    microservice.setServiceName(registration.getServiceId());
    microservice.setVersion(registration.getVersion());
    microservice.setFramework(new Framework());
    microservice.setEnvironment(registration.getEnvironment());
    return microservice;
  }
}
