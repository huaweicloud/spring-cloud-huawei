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

package org.springframework.cloud.servicecomb.discovery.discovery;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;

public class ServiceCombDiscoveryClient implements DiscoveryClient {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombDiscoveryClient.class);

  @Autowired
  ServiceCombClient serviceCombClient;

  private ServiceCombDiscoveryProperties discoveryProperties;

  public ServiceCombDiscoveryClient(ServiceCombDiscoveryProperties discoveryProperties) {
    this.discoveryProperties = discoveryProperties;
    LOGGER.info("init ServiceCombDiscoveryClient " + this.discoveryProperties);
  }

  @Override
  public String description() {
    return "this is servicecomb implement";
  }

  @Override
  public List<ServiceInstance> getInstances(String serviceId) {
    Microservice microService = MicroserviceHandler
        .createMicroservice(discoveryProperties, serviceId);
    return MicroserviceHandler
        .getInstances(discoveryProperties, microService,
            serviceCombClient);//spring cloud serviceId equals servicecomb serviceName
  }

  @Override
  public List<String> getServices() {
    //TODO
    return null;
  }
}
