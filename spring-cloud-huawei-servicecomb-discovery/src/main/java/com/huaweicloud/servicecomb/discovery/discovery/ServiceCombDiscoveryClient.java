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
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceResponse;

public class ServiceCombDiscoveryClient implements DiscoveryClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombDiscoveryClient.class);

  private ServiceCombClient serviceCombClient;

  private ServiceCombDiscoveryProperties discoveryProperties;

  public ServiceCombDiscoveryClient(ServiceCombDiscoveryProperties discoveryProperties,
      ServiceCombClient serviceCombClient) {
    this.discoveryProperties = discoveryProperties;
    this.serviceCombClient = serviceCombClient;
  }

  @Override
  public String description() {
    return "this is servicecomb implement";
  }

  @Override
  public ServiceInstance getLocalServiceInstance() {
    return null;
  }

  @Override
  public List<ServiceInstance> getInstances(String serviceId) {
    Microservice microService = MicroserviceHandler
        .createMicroservice(discoveryProperties, serviceId);
    //spring cloud serviceId equals servicecomb serviceName
    return MicroserviceHandler.getInstances(microService, serviceCombClient);
  }

  @Override
  public List<String> getServices() {
    List<String> serviceList = new ArrayList<>();
    try {
      MicroserviceResponse microServiceResponse = serviceCombClient
          .getServices();
      if (microServiceResponse == null || microServiceResponse.getServices() == null) {
        return serviceList;
      }
      for (Microservice microservice : microServiceResponse.getServices()) {
        serviceList.add(microservice.getServiceName());
      }
      return serviceList;
    } catch (ServiceCombException e) {
      LOGGER.error("getServices failed", e);
    }
    return serviceList;
  }
}
