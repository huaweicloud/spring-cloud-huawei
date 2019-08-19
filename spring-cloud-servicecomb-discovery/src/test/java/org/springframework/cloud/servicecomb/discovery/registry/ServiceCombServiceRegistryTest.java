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

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.exception.ServiceCombException;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;
import org.springframework.cloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import mockit.Tested;
import mockit.Verifications;
import mockit.integration.junit4.JMockit;

/**
 * @Author wangqijun
 * @Date 10:48 2019-07-19
 **/
@RunWith(JMockit.class)
public class ServiceCombServiceRegistryTest {

  @Injectable
  ServiceCombClient serviceCombClient;

  @Injectable
  HeartbeatScheduler heartbeatScheduler;

  @Injectable
  ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  @Mocked
  ServiceCombRegistration registration;

  @Test
  public void hasRegisterMicroservice() throws ServiceCombException {
    new Expectations() {
      {
        serviceCombClient.getServiceId((Microservice) any);
        result = "1";

        serviceCombClient.registerInstance((MicroserviceInstance) any);
        result = "2";
      }
    };
    ServiceCombServiceRegistry serviceCombServiceRegistry = new ServiceCombServiceRegistry(serviceCombClient,
        heartbeatScheduler, serviceCombDiscoveryProperties);
    serviceCombServiceRegistry.register(registration);
    Assert.assertEquals(serviceCombServiceRegistry.getInstanceID(), "2");
    Assert.assertEquals(serviceCombServiceRegistry.getServiceID(), "1");
  }

  @Test
  public void neverRegisterMicroservice() throws ServiceCombException {
    new Expectations() {
      {
        serviceCombClient.registerMicroservice((Microservice) any);
        result = "4";
      }
    };
    ServiceCombServiceRegistry serviceCombServiceRegistry = new ServiceCombServiceRegistry(serviceCombClient,
        heartbeatScheduler, serviceCombDiscoveryProperties);
    serviceCombServiceRegistry.register(registration);
    Assert.assertEquals(serviceCombServiceRegistry.getInstanceID(), null);
    Assert.assertEquals(serviceCombServiceRegistry.getServiceID(), "4");
  }
}