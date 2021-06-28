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

package com.huaweicloud.servicecomb.discovery.ribbon;

import java.util.ArrayList;
import java.util.List;

import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.Server;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

/**
 * @Author wangqijun
 * @Date 11:36 2019-08-16
 **/
public class ServiceCombServerListTest {

  @Tested
  ServiceCombServerList serviceCombServerList;

  @Injectable
  DiscoveryClient discoveryClient;

  @Injectable
  IClientConfig iClientConfig;

  @Test
  public void getInitialListOfServers() throws ServiceCombException {
    List<ServiceInstance> instanceList = new ArrayList<>();
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setServiceId("serviceid11");
    microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
    ServiceInstance serviceInstance = new ServiceCombServiceInstance(microserviceInstance);
    instanceList.add(serviceInstance);
    new Expectations() {
      {
        iClientConfig.getClientName();
        result = "serviceid11";
        discoveryClient.getInstances("serviceid11");
        result = instanceList;
      }
    };

    serviceCombServerList.initWithNiwsConfig(iClientConfig);
    List<Server> serverList = serviceCombServerList.getUpdatedListOfServers();
    Assert.assertEquals(serverList.size(), 1);
  }
}