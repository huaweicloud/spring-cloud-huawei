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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.EnvironmentConfiguration;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.HealthCheck;
import org.apache.servicecomb.service.center.client.model.HealthCheckMode;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceStatus;
import org.springframework.util.StringUtils;

import com.huaweicloud.common.util.NetUtil;
import com.huaweicloud.servicecomb.discovery.client.model.DiscoveryConstants;
import com.huaweicloud.servicecomb.discovery.registry.TagsProperties;

public class MicroserviceHandler {
  private static final String SERVICE_MAPPING = "SERVICE_MAPPING";

  private static final String VERSION_MAPPING = "VERSION_MAPPING";

  private static final String APP_MAPPING = "APP_MAPPING";

  private static final String CAS_APPLICATION_ID = "CAS_APPLICATION_ID";

  private static final String CAS_COMPONENT_NAME = "CAS_COMPONENT_NAME";

  private static final String CAS_INSTANCE_VERSION = "CAS_INSTANCE_VERSION";

  private static final String CAS_INSTANCE_ID = "CAS_INSTANCE_ID";

  private static final String CAS_ENVIRONMENT_ID = "CAS_ENVIRONMENT_ID";

  public static Microservice createMicroservice(ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    Microservice microservice = new Microservice();

    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    if (!StringUtils.isEmpty(envConfig.getString(APP_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(APP_MAPPING)))) {
      microservice.setAppId(envConfig.getString(envConfig.getString(APP_MAPPING)));
    } else {
      microservice.setAppId(serviceCombDiscoveryProperties.getAppName());
    }
    if (!StringUtils.isEmpty(envConfig.getString(SERVICE_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(SERVICE_MAPPING)))) {
      microservice.setServiceName(envConfig.getString(envConfig.getString(SERVICE_MAPPING)));
    } else {
      microservice.setServiceName(serviceCombDiscoveryProperties.getServiceName());
    }
    if (!StringUtils.isEmpty(envConfig.getString(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(VERSION_MAPPING)))) {
      microservice.setVersion(envConfig.getString(envConfig.getString(VERSION_MAPPING)));
    } else {
      microservice.setVersion(serviceCombDiscoveryProperties.getVersion());
    }
    microservice.setEnvironment(serviceCombDiscoveryProperties.getEnvironment());

    Framework framework = createFramework();
    microservice.setFramework(framework);
    if (serviceCombDiscoveryProperties.isAllowCrossApp()) {
      microservice.getProperties().put(DiscoveryConstants.CONFIG_ALLOW_CROSS_APP_KEY, "true");
    }
    microservice.setStatus(MicroserviceStatus.UP);
    return microservice;
  }

  private static Framework createFramework() {
    Framework framework = new Framework();
    framework.setName("springCloud");
    framework.setVersion(MicroserviceHandler.class.getPackage().getImplementationVersion());
    return framework;
  }

  public static MicroserviceInstance createMicroserviceInstance(
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      TagsProperties tagsProperties) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setHostName(NetUtil.getLocalHost());
    if (null != serviceCombDiscoveryProperties.getDatacenter()) {
      microserviceInstance.setDataCenterInfo(serviceCombDiscoveryProperties.getDatacenter());
    }
    List<String> endPoints = new ArrayList<>();
    String address;
    if (StringUtils.isEmpty(serviceCombDiscoveryProperties.getServerAddress())) {
      address = NetUtils.getHostAddress();
    } else {
      address = serviceCombDiscoveryProperties.getServerAddress();
    }
    endPoints.add("rest://" + address + ":" + serviceCombDiscoveryProperties.getPort());
    microserviceInstance.setEndpoints(endPoints);
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.pull);
    healthCheck.setInterval(serviceCombDiscoveryProperties.getHealthCheckInterval());
    healthCheck.setTimes(3);
    microserviceInstance.setHealthCheck(healthCheck);
    String currTime = String.valueOf(System.currentTimeMillis());
    microserviceInstance.setTimestamp(currTime);
    microserviceInstance.setModTimestamp(currTime);

    EnvironmentConfiguration envConfig = new EnvironmentConfiguration();
    if (!StringUtils.isEmpty(envConfig.getString(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(envConfig.getString(envConfig.getString(VERSION_MAPPING)))) {
      microserviceInstance.setVersion(envConfig.getString(VERSION_MAPPING));
    } else {
      microserviceInstance.setVersion(serviceCombDiscoveryProperties.getVersion());
    }

    Map<String, String> properties = new HashMap<>();
    if (tagsProperties.getTag() != null) {
      properties.putAll(tagsProperties.getTag());
    }
    properties.putAll(genCasProperties());
    microserviceInstance.setProperties(properties);
    return microserviceInstance;
  }

  private static Map<String, String> genCasProperties() {
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
