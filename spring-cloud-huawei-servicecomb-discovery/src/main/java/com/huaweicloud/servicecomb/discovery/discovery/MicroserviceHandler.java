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

import java.util.Collections;
import java.util.List;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.Framework;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceStatus;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceRegistryConfig;

/**
 * @Author wangqijun
 * @Date 12:26 2019-07-17
 **/
public class MicroserviceHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(MicroserviceHandler.class);

  private static List<ServiceInstance> instanceList = Collections.emptyList();

  public static final Map<String, String> serviceRevision = new ConcurrentHashMap<>();

  public static List<ServiceInstance> getInstances(Microservice microservice, ServiceCombClient serviceCombClient) {
    try {
      String revision = "0";
      if (serviceRevision.containsKey(microservice.getServiceName())) {
        revision = serviceRevision.get(microservice.getServiceName());
      }
      instanceList = serviceCombClient.getInstances(microservice, revision);
    } catch (ServiceCombException e) {
      LOGGER.warn("get instances failed.", e);
    }
    return instanceList;
  }

  public static Microservice createMicroservice(ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      String serviceName) {
    Microservice microservice = new Microservice();
    microservice.setServiceName(serviceName);
    microservice.setVersion(ServiceRegistryConfig.DEFAULT_CALL_VERSION);
    microservice.setFramework(new Framework());
    if (!serviceCombDiscoveryProperties.isAllowCrossApp()) {
      microservice.setAppId(serviceCombDiscoveryProperties.getAppName());
    }
    microservice.setStatus(MicroserviceStatus.UP);
    return microservice;
  }
}
