/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.apache.servicecomb.service.center.client.model.Framework;
import org.apache.servicecomb.service.center.client.model.HealthCheck;
import org.apache.servicecomb.service.center.client.model.HealthCheckMode;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.apache.servicecomb.service.center.client.model.MicroserviceStatus;
import org.springframework.core.env.Environment;

import com.huaweicloud.common.util.NetUtil;
import com.huaweicloud.service.engine.common.configration.bootstrap.BootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.DiscoveryBootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.InstanceProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.MicroserviceProperties;
import com.huaweicloud.servicecomb.discovery.client.model.DiscoveryConstants;

public class MicroserviceHandler {
  private static final String SERVICE_MAPPING = "SERVICE_MAPPING";

  private static final String VERSION_MAPPING = "VERSION_MAPPING";

  private static final String APP_MAPPING = "APP_MAPPING";

  private static final String CAS_APPLICATION_ID = "CAS_APPLICATION_ID";

  private static final String CAS_COMPONENT_NAME = "CAS_COMPONENT_NAME";

  private static final String CAS_INSTANCE_VERSION = "CAS_INSTANCE_VERSION";

  private static final String CAS_INSTANCE_ID = "CAS_INSTANCE_ID";

  private static final String CAS_ENVIRONMENT_ID = "CAS_ENVIRONMENT_ID";

  private static final String SERVICE_PROPS = "SERVICECOMB_SERVICE_PROPS";

  private static final String INSTANCE_PROPS = "SERVICECOMB_INSTANCE_PROPS";

  private static String instanceAddress;

  public static Microservice createMicroservice(BootstrapProperties bootstrapProperties, Environment environment) {
    DiscoveryBootstrapProperties discoveryBootstrapProperties = bootstrapProperties.getDiscoveryBootstrapProperties();
    MicroserviceProperties microserviceProperties = bootstrapProperties.getMicroserviceProperties();
    Microservice microservice = new Microservice();
    microservice.setProperties(microserviceProperties.getProperties());

    if (discoveryBootstrapProperties.isAllowCrossApp()) {
      microservice.setAlias(microserviceProperties.getApplication() +
          DiscoveryConstants.APP_SERVICE_SEPRATOR + microserviceProperties.getName());
    }
    if (!StringUtils.isEmpty(environment.getProperty(APP_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(APP_MAPPING)))) {
      microservice.setAppId(environment.getProperty(environment.getProperty(APP_MAPPING)));
    } else {
      microservice.setAppId(microserviceProperties.getApplication());
    }
    if (!StringUtils.isEmpty(environment.getProperty(SERVICE_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(SERVICE_MAPPING)))) {
      microservice.setServiceName(environment.getProperty(environment.getProperty(SERVICE_MAPPING)));
    } else {
      microservice.setServiceName(microserviceProperties.getName());
    }
    if (!StringUtils.isEmpty(environment.getProperty(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(VERSION_MAPPING)))) {
      microservice.setVersion(environment.getProperty(environment.getProperty(VERSION_MAPPING)));
    } else {
      microservice.setVersion(microserviceProperties.getVersion());
    }
    microservice.setEnvironment(microserviceProperties.getEnvironment());

    Framework framework = createFramework();
    microservice.setFramework(framework);
    if (discoveryBootstrapProperties.isAllowCrossApp()) {
      microservice.getProperties().put(DiscoveryConstants.CONFIG_ALLOW_CROSS_APP_KEY, "true");
    }

    String[] servicePropArray = parseArrayValue(environment.getProperty(SERVICE_PROPS)).toArray(new String[0]);
    if (servicePropArray.length != 0) {
      microservice.getProperties().putAll(parseProps(servicePropArray));
    }

    microservice.setStatus(MicroserviceStatus.UP);
    return microservice;
  }

  public static List<String> parseArrayValue(String value) {
    if (value == null) {
      return new ArrayList<>(0);
    }
    String[] tokens = value.split(",");
    List<String> result = new ArrayList<>(tokens.length);
    for (String t : tokens) {
      result.add(t.trim());
    }
    return result;
  }

  private static Framework createFramework() {
    Framework framework = new Framework();
    framework.setName("springCloud");
    framework.setVersion(MicroserviceHandler.class.getPackage().getImplementationVersion());
    return framework;
  }

  public static MicroserviceInstance createMicroserviceInstance(BootstrapProperties bootstrapProperties,
      DiscoveryProperties discoveryProperties, Environment environment) {
    DiscoveryBootstrapProperties discoveryBootstrapProperties = bootstrapProperties.getDiscoveryBootstrapProperties();
    MicroserviceProperties microserviceProperties = bootstrapProperties.getMicroserviceProperties();
    InstanceProperties instanceProperties = bootstrapProperties.getInstanceProperties();

    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    String hostName = StringUtils.isEmpty(discoveryBootstrapProperties.getHostname()) ? NetUtil.getLocalHost()
        : discoveryBootstrapProperties.getHostname();
    microserviceInstance.setHostName(hostName.length() > 64 ? hostName.substring(0, 64) : hostName);
    if (null != discoveryBootstrapProperties.getDatacenter()) {
      microserviceInstance.setDataCenterInfo(discoveryBootstrapProperties.getDatacenter());
    }
    List<String> endPoints = new ArrayList<>();
    String address;
    if (StringUtils.isEmpty(discoveryBootstrapProperties.getPublishAddress())) {
      address = discoveryBootstrapProperties.isPreferIpv6Address() ?
              NetUtils.getIpv6HostAddress() : NetUtils.getHostAddress();
    } else {
      address = discoveryBootstrapProperties.getPublishAddress();
    }
    instanceAddress = address;
    if (discoveryProperties.isSslEnabled()) {
      endPoints.add("rest://" + address + ":" + discoveryProperties.getPort() + "?sslEnabled="
          + discoveryProperties.isSslEnabled());
    } else {
      endPoints.add("rest://" + address + ":" + discoveryProperties.getPort());
    }
    microserviceInstance.setEndpoints(endPoints);
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.push);
    healthCheck.setInterval(discoveryBootstrapProperties.getHealthCheckInterval());
    healthCheck.setTimes(3);
    microserviceInstance.setHealthCheck(healthCheck);
    String currTime = String.valueOf(System.currentTimeMillis());
    microserviceInstance.setTimestamp(currTime);
    microserviceInstance.setModTimestamp(currTime);

    // what's MicroserviceInstance doing? same sa Microservice?
    if (!StringUtils.isEmpty(environment.getProperty(VERSION_MAPPING)) &&
        !StringUtils.isEmpty(environment.getProperty(environment.getProperty(VERSION_MAPPING)))) {
      microserviceInstance.setVersion(environment.getProperty(VERSION_MAPPING));
    } else {
      microserviceInstance.setVersion(microserviceProperties.getVersion());
    }

    Map<String, String> properties = new HashMap<>();
    properties.putAll(instanceProperties.getProperties());
    properties.putAll(genCasProperties(environment));
    microserviceInstance.setProperties(properties);

    if (StringUtils.isNotEmpty(instanceProperties.getInitialStatus())) {
      microserviceInstance.setStatus(MicroserviceInstanceStatus.valueOf(instanceProperties.getInitialStatus()));
    }
    return microserviceInstance;
  }

  private static Map<String, String> genCasProperties(Environment environment) {
    Map<String, String> properties = new HashMap<>();
    if (!StringUtils.isEmpty(environment.getProperty(CAS_APPLICATION_ID))) {
      properties.put(CAS_APPLICATION_ID, environment.getProperty(CAS_APPLICATION_ID));
    }
    if (!StringUtils.isEmpty(environment.getProperty(CAS_COMPONENT_NAME))) {
      properties.put(CAS_COMPONENT_NAME, environment.getProperty(CAS_COMPONENT_NAME));
    }
    if (!StringUtils.isEmpty(environment.getProperty(CAS_INSTANCE_VERSION))) {
      properties.put(CAS_INSTANCE_VERSION, environment.getProperty(CAS_INSTANCE_VERSION));
    }
    if (!StringUtils.isEmpty(environment.getProperty(CAS_INSTANCE_ID))) {
      properties.put(CAS_INSTANCE_ID, environment.getProperty(CAS_INSTANCE_ID));
    }
    if (!StringUtils.isEmpty(environment.getProperty(CAS_ENVIRONMENT_ID))) {
      properties.put(CAS_ENVIRONMENT_ID, environment.getProperty(CAS_ENVIRONMENT_ID));
    }

    String[] instancePropArray = parseArrayValue(environment.getProperty(INSTANCE_PROPS)).toArray(new String[0]);
    if (instancePropArray.length != 0) {
      properties.putAll(parseProps(instancePropArray));
    }

    return properties;
  }

  private static Map<String, String> parseProps(String... value) {
    return Arrays.stream(value).map(v -> v.split(":"))
        .filter(v -> v.length == 2)
        .collect(Collectors.toMap(v -> v[0], v -> v[1]));
  }

  public static String getInstanceAddress() {
    return instanceAddress;
  }
}
