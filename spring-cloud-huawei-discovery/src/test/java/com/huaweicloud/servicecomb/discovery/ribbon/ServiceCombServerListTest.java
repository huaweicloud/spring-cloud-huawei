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

import org.apache.servicecomb.service.center.client.model.DataCenterInfo;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.common.transport.DiscoveryBootstrapProperties;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;
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
  ServiceCombRegistration serviceCombRegistration;

  @Injectable
  IClientConfig iClientConfig;

  @Test
  public void getInitialListOfServersTest() throws ServiceCombException {
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

  @Test
  public void regionAndAZMatchTest() throws ServiceCombException {

    List<ServiceInstance> instanceList = new ArrayList<>();
    DataCenterInfo dataCenterInfo = new DataCenterInfo();
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();

    dataCenterInfo.setName("testName");
    dataCenterInfo.setRegion("cn-test-1");
    dataCenterInfo.setAvailableZone("availableZone1");

    microserviceInstance.setServiceId("serviceId1");
    microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
    microserviceInstance.setDataCenterInfo(dataCenterInfo);

    DiscoveryBootstrapProperties discoveryBootstrapProperties = new DiscoveryBootstrapProperties();
    discoveryBootstrapProperties.setEnableZoneAware(true);

    new Expectations() {
      {
        iClientConfig.getClientName();
        result = "serviceId1";
        discoveryClient.getInstances("serviceId1");
        result = instanceList;
        serviceCombRegistration.getMicroserviceInstance();
        result = microserviceInstance;
        serviceCombRegistration.getDiscoveryBootstrapProperties();
        result = discoveryBootstrapProperties;
      }
    };

    DataCenterInfo dataCenterInfo1 = new DataCenterInfo();
    MicroserviceInstance Instance1 = new MicroserviceInstance();

    dataCenterInfo1.setName("testName");
    dataCenterInfo1.setRegion("cn-test-1");
    dataCenterInfo1.setAvailableZone("availableZone1");

    Instance1.setServiceId("serviceId2");
    Instance1.setStatus(MicroserviceInstanceStatus.UP);
    Instance1.setDataCenterInfo(dataCenterInfo1);
    instanceList.add(new ServiceCombServiceInstance(Instance1));

    DataCenterInfo dataCenterInfo2 = new DataCenterInfo();
    MicroserviceInstance Instance2 = new MicroserviceInstance();

    dataCenterInfo2.setName("testName");
    dataCenterInfo2.setRegion("cn-test-1");
    dataCenterInfo2.setAvailableZone("availableZone3");

    Instance2.setServiceId("serviceId3");
    Instance2.setStatus(MicroserviceInstanceStatus.UP);
    Instance2.setDataCenterInfo(dataCenterInfo2);
    instanceList.add(new ServiceCombServiceInstance(Instance2));

    serviceCombServerList.initWithNiwsConfig(iClientConfig);
    List<Server> serverList = serviceCombServerList.getUpdatedListOfServers();
    Assert.assertEquals(serverList.size(), 1);
    Assert.assertEquals(serverList.get(0).getZone(), "availableZone1");
  }

  @Test
  public void regionMatchTest() throws ServiceCombException {

    List<ServiceInstance> instanceList = new ArrayList<>();
    DataCenterInfo dataCenterInfo = new DataCenterInfo();
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();

    dataCenterInfo.setName("testName");
    dataCenterInfo.setRegion("cn-test-1");
    dataCenterInfo.setAvailableZone("availableZone1");

    microserviceInstance.setServiceId("serviceId1");
    microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
    microserviceInstance.setDataCenterInfo(dataCenterInfo);

    DiscoveryBootstrapProperties discoveryBootstrapProperties = new DiscoveryBootstrapProperties();
    discoveryBootstrapProperties.setEnableZoneAware(true);

    new Expectations() {
      {
        iClientConfig.getClientName();
        result = "serviceId1";
        discoveryClient.getInstances("serviceId1");
        result = instanceList;
        serviceCombRegistration.getMicroserviceInstance();
        result = microserviceInstance;
        serviceCombRegistration.getDiscoveryBootstrapProperties();
        result = discoveryBootstrapProperties;
      }
    };

    DataCenterInfo dataCenterInfo1 = new DataCenterInfo();
    MicroserviceInstance Instance1 = new MicroserviceInstance();

    dataCenterInfo1.setName("testName");
    dataCenterInfo1.setRegion("cn-test-1");
    dataCenterInfo1.setAvailableZone("availableZone2");

    Instance1.setServiceId("serviceId2");
    Instance1.setStatus(MicroserviceInstanceStatus.UP);
    Instance1.setDataCenterInfo(dataCenterInfo1);
    instanceList.add(new ServiceCombServiceInstance(Instance1));

    DataCenterInfo dataCenterInfo2 = new DataCenterInfo();
    MicroserviceInstance Instance2 = new MicroserviceInstance();

    dataCenterInfo2.setName("testName");
    dataCenterInfo2.setRegion("cn-test-3");
    dataCenterInfo2.setAvailableZone("availableZone3");

    Instance2.setServiceId("serviceId3");
    Instance2.setStatus(MicroserviceInstanceStatus.UP);
    Instance2.setDataCenterInfo(dataCenterInfo2);
    instanceList.add(new ServiceCombServiceInstance(Instance2));

    serviceCombServerList.initWithNiwsConfig(iClientConfig);
    List<Server> serverList = serviceCombServerList.getUpdatedListOfServers();
    Assert.assertEquals(serverList.size(), 1);
    Assert.assertEquals(serverList.get(0).getZone(), "availableZone2");
  }

  @Test
  public void onMatchTest() throws ServiceCombException {
    List<ServiceInstance> instanceList = new ArrayList<>();
    DataCenterInfo dataCenterInfo = new DataCenterInfo();
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();

    dataCenterInfo.setName("testName");
    dataCenterInfo.setRegion("cn-test-1");
    dataCenterInfo.setAvailableZone("availableZone1");

    microserviceInstance.setServiceId("serviceId1");
    microserviceInstance.setStatus(MicroserviceInstanceStatus.UP);
    microserviceInstance.setDataCenterInfo(dataCenterInfo);

    DiscoveryBootstrapProperties discoveryBootstrapProperties = new DiscoveryBootstrapProperties();
    discoveryBootstrapProperties.setEnableZoneAware(true);

    new Expectations() {
      {
        iClientConfig.getClientName();
        result = "serviceId1";
        discoveryClient.getInstances("serviceId1");
        result = instanceList;
        serviceCombRegistration.getMicroserviceInstance();
        result = microserviceInstance;
        serviceCombRegistration.getDiscoveryBootstrapProperties();
        result = discoveryBootstrapProperties;
      }
    };

    DataCenterInfo dataCenterInfo1 = new DataCenterInfo();
    MicroserviceInstance Instance1 = new MicroserviceInstance();

    dataCenterInfo1.setName("testName");
    dataCenterInfo1.setRegion("cn-test-2");
    dataCenterInfo1.setAvailableZone("availableZone2");

    Instance1.setServiceId("serviceId2");
    Instance1.setStatus(MicroserviceInstanceStatus.UP);
    Instance1.setDataCenterInfo(dataCenterInfo1);
    instanceList.add(new ServiceCombServiceInstance(Instance1));

    DataCenterInfo dataCenterInfo2 = new DataCenterInfo();
    MicroserviceInstance Instance2 = new MicroserviceInstance();

    dataCenterInfo2.setName("testName");
    dataCenterInfo2.setRegion("cn-test-3");
    dataCenterInfo2.setAvailableZone("availableZone3");

    Instance2.setServiceId("serviceId3");
    Instance2.setStatus(MicroserviceInstanceStatus.UP);
    Instance2.setDataCenterInfo(dataCenterInfo2);
    instanceList.add(new ServiceCombServiceInstance(Instance2));

    serviceCombServerList.initWithNiwsConfig(iClientConfig);
    List<Server> serverList = serviceCombServerList.getUpdatedListOfServers();
    Assert.assertEquals(serverList.size(), 2);
    Assert.assertEquals(serverList.get(0).getZone(), "availableZone2");
    Assert.assertEquals(serverList.get(1).getZone(), "availableZone3");
  }
}