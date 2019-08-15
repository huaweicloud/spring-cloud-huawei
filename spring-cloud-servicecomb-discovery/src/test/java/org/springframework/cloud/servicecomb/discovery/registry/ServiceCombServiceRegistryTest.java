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

package org.springframework.cloud.servicecomb.discovery.registry;

import org.junit.Test;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.exception.ServiceCombException;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;

import mockit.Injectable;
import mockit.Tested;

/**
 * @Author wangqijun
 * @Date 10:48 2019-07-19
 **/
public class ServiceCombServiceRegistryTest {

  @Tested
  private ServiceCombServiceRegistry serviceCombServiceRegistry;

  @Test
  public void register(@Injectable ServiceCombClient serviceCombClient,
      @Injectable MicroserviceInstance microserviceInstance, @Injectable Microservice microservice,
      @Injectable ServiceCombRegistration registration)
      throws ServiceCombException {
//    new Expectations() {
//      {
//        new ServiceCombServiceRegistry(serviceCombClient,h);
//        result=serviceCombServiceRegistry;
//
//        serviceCombClient.getServiceId(microservice);
//        result = "1";
//
//        serviceCombClient.registerInstance(microserviceInstance);
//        result = "2";
//
//      }
//    };
//
//    serviceCombServiceRegistry.register(registration);
//    new Verifications() {
//      {
//        serviceCombServiceRegistry.register(registration);
//
//        times = 1;
//      }
//    };
  }

  @Test
  public void deregister() {
  }

  @Test
  public void close() {
  }

  @Test
  public void setStatus() {
  }

  @Test
  public void getStatus() {
  }
}