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
import java.util.HashMap;
import java.util.List;

import java.util.Map;
import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.servicecomb.foundation.common.net.NetUtils;
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
import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 15:18 2019-08-08
 **/
public class RegistryHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RegistryHandler.class);

  private static final String CAS_APPLICATION_ID = "CAS_APPLICATION_ID";

  private static final String CAS_COMPONENT_NAME = "CAS_COMPONENT_NAME";

  private static final String CAS_INSTANCE_VERSION = "CAS_INSTANCE_VERSION";

  private static final String CAS_INSTANCE_ID = "CAS_INSTANCE_ID";

  private static final String CAS_ENVIRONMENT_ID = "CAS_ENVIRONMENT_ID";

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
    if (null != serviceCombDiscoveryProperties.getDatacenter()) {
      microserviceInstance.setDataCenterInfo(serviceCombDiscoveryProperties.getDatacenter());
    }
    List<String> endPoints = new ArrayList<>();
    String address = NetUtils.getHostAddress();
    endPoints.add("rest://" + address + ":" + serviceCombDiscoveryProperties.getPort());
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
    Map<String, String> properties = new HashMap<>();
    if (tagsProperties.getTag() != null) {
      properties.putAll(tagsProperties.getTag());
    }
    properties.putAll(genCasProperties());
    microserviceInstance.setProperties(properties);
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

  public static Map<String, String> genCasProperties() {
    Map<String, String> properties = new HashMap<>();
    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    if (!StringUtils.isEmpty(envConfig.getString(CAS_APPLICATION_ID))) {
      properties.put(CAS_APPLICATION_ID, envConfig.getString(CAS_APPLICATION_ID));
    }
    if (!StringUtils.isEmpty(envConfig.getString(CAS_COMPONENT_NAME))) {
      properties.put(CAS_COMPONENT_NAME, envConfig.getString(CAS_COMPONENT_NAME));
    }
    if (!StringUtils.isEmpty(envConfig.getString(CAS_INSTANCE_VERSION))) {
      properties.put(CAS_INSTANCE_VERSION, envConfig.getString(CAS_INSTANCE_VERSION));
    }
    if (!StringUtils.isEmpty(envConfig.getString(CAS_INSTANCE_ID))) {
      properties.put(CAS_INSTANCE_ID, envConfig.getString(CAS_INSTANCE_ID));
    }
    if (!StringUtils.isEmpty(envConfig.getString(CAS_ENVIRONMENT_ID))) {
      properties.put(CAS_ENVIRONMENT_ID, envConfig.getString(CAS_ENVIRONMENT_ID));
    }
    return properties;
  }
}
