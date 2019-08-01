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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.exception.ServiceCombException;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;

/**
 * @Author wangqijun
 * @Date 09:57 2019-07-31
 **/
public class ServiceCombDiscoveryClientTest {

  @Tested
  ServiceCombDiscoveryClient serviceCombDiscoveryClient;

  @Mocked
  ServiceCombClient serviceCombClient;

  @Mocked
  ServiceCombDiscoveryProperties discoveryProperties;

  @Mocked
  Microservice microservice;

  @Test
  public void getInstances(@Injectable ServiceCombClient serviceCombClient,
      @Injectable ServiceCombDiscoveryProperties discoveryProperties, @Injectable Microservice microservice)
      throws ServiceCombException {
    new Expectations(MicroserviceHandler.class) {
      {
        ServiceCombDiscoveryProperties serviceCombDiscoveryProperties = new ServiceCombDiscoveryProperties();
        serviceCombDiscoveryProperties.setAppName("test");
        serviceCombDiscoveryProperties.setServiceName("testservice");
        serviceCombDiscoveryProperties.setVersion("0.1");

        new ServiceCombDiscoveryClient(serviceCombDiscoveryProperties);
        result = serviceCombDiscoveryClient;
//        List<ServiceInstance> serviceInstanceList=new ArrayList<>();
//        serviceInstanceList.add(
//            new DefaultServiceInstance("111", "1", "127.0.0.1", 1000, false));
//        serviceCombClient.getInstances(microservice);
//        result = serviceInstanceList;
//        MicroserviceHandler.getInstances(serviceCombDiscoveryProperties,"testservice",serviceCombClient);
//        result = serviceInstanceList;
      }
    };
    serviceCombDiscoveryClient.getInstances("1");
  }
}