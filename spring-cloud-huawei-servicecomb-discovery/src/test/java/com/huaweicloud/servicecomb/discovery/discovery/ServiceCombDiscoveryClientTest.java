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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.ServiceInstance;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceResponse;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

/**
 * @Author wangqijun
 * @Date 09:57 2019-07-31
 **/

public class ServiceCombDiscoveryClientTest {

  @Tested
  ServiceCombDiscoveryClient serviceCombDiscoveryClient;

  @Test
  public void getInstances(@Injectable ServiceCombClient serviceCombClient,
      @Injectable ServiceCombDiscoveryProperties serviceCombDiscoveryProperties)
      throws ServiceCombException {
    serviceCombDiscoveryProperties = new ServiceCombDiscoveryProperties();
    serviceCombDiscoveryProperties.setAppName("test");
    serviceCombDiscoveryProperties.setServiceName("testservice");
    serviceCombDiscoveryProperties.setVersion("latest");
    List<ServiceInstance> serviceInstanceList = new ArrayList<>();
    serviceInstanceList.add(
        new DefaultServiceInstance("1", "127.0.0.1", 1000, false));

    Microservice microservice = new Microservice();
    microservice.setServiceName("testservice");
    new Expectations(MicroserviceHandler.class) {
      {
        MicroserviceHandler.createMicroservice((ServiceCombDiscoveryProperties) any, anyString);
        result = microservice;
        MicroserviceHandler.getInstances((Microservice) any, (ServiceCombClient) any);
        result = serviceInstanceList;
      }
    };
    ServiceCombDiscoveryClient serviceCombDiscoveryClient = new ServiceCombDiscoveryClient(
        serviceCombDiscoveryProperties, serviceCombClient);
    List<ServiceInstance> actual = serviceCombDiscoveryClient.getInstances("testservice");
    Assert.assertEquals(1, actual.size());
  }

  @Test
  public void getServices(@Injectable ServiceCombClient serviceCombClient,
      @Injectable ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) throws ServiceCombException {
    MicroserviceResponse microserviceResponse = new MicroserviceResponse();
    Microservice microservice = new Microservice();
    microservice.setServiceName("test");
    List<Microservice> microserviceList = new ArrayList<>();
    microserviceList.add(microservice);
    microserviceResponse.setServices(microserviceList);
    new Expectations() {
      {
        serviceCombClient.getServices();
        result = microserviceResponse;
      }
    };
    ServiceCombDiscoveryClient serviceCombDiscoveryClient = new ServiceCombDiscoveryClient(
        serviceCombDiscoveryProperties, serviceCombClient);
    List<String> actual = serviceCombDiscoveryClient.getServices();
    Assert.assertEquals(actual.size(), 1);
  }
}