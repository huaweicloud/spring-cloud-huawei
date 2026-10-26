/*

 * Copyright (C) 2020-2026 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.nacos.discovery.discovery.reactive;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.alibaba.nacos.api.exception.NacosException;
import com.huaweicloud.nacos.discovery.NacosServiceInstance;
import com.huaweicloud.nacos.discovery.discovery.NacosDiscovery;

import org.springframework.cloud.client.ServiceInstance;

public class NacosReactiveDiscoveryClientTest {

  private NacosDiscovery nacosDiscovery;

  private NacosReactiveDiscoveryClient nacosReactiveDiscoveryClient;

  @BeforeEach
  public void setUp() {
    nacosDiscovery = Mockito.mock(NacosDiscovery.class);
    nacosReactiveDiscoveryClient = new NacosReactiveDiscoveryClient(nacosDiscovery);
  }

  // Test scenario: description() returns the predefined constant string "Nacos Reactive Discovery Client"
  @Test
  public void descriptionReturnsConstantDescription() {
    String result = nacosReactiveDiscoveryClient.description();

    Assertions.assertEquals("Nacos Reactive Discovery Client", result);
  }

  // Test scenario: getInstances() emits service instances when NacosDiscovery.getInstances() succeeds
  @Test
  public void getInstancesReturnsInstancesWhenDiscoverySucceeds() throws NacosException {
    NacosServiceInstance instance1 = new NacosServiceInstance();
    instance1.setServiceId("service-A");
    instance1.setHost("10.0.0.1");
    instance1.setPort(8080);

    NacosServiceInstance instance2 = new NacosServiceInstance();
    instance2.setServiceId("service-A");
    instance2.setHost("10.0.0.2");
    instance2.setPort(8081);

    List<ServiceInstance> expectedInstances = Arrays.asList(instance1, instance2);
    Mockito.when(nacosDiscovery.getInstances("service-A")).thenReturn(expectedInstances);
    List<ServiceInstance> result = nacosReactiveDiscoveryClient.getInstances("service-A")
        .collectList().block();

    Assertions.assertNotNull(result);
    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals("service-A", result.get(0).getServiceId());
    Assertions.assertEquals("10.0.0.1", result.get(0).getHost());
    Assertions.assertEquals(8080, result.get(0).getPort());
    Assertions.assertEquals("service-A", result.get(1).getServiceId());
    Assertions.assertEquals("10.0.0.2", result.get(1).getHost());
    Assertions.assertEquals(8081, result.get(1).getPort());
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getInstances("service-A");
  }

  // Test scenario: getInstances() completes with no emissions when NacosDiscovery.getInstances() throws NacosException
  @Test
  public void getInstancesCompletesEmptyWhenNacosExceptionThrown() throws NacosException {
    NacosException nacosException = new NacosException(NacosException.SERVER_ERROR, "nacos server error");
    Mockito.when(nacosDiscovery.getInstances("failing-service")).thenThrow(nacosException);
    List<ServiceInstance> result = nacosReactiveDiscoveryClient.getInstances("failing-service")
        .collectList().block();

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getInstances("failing-service");
  }

  // Test scenario: getInstances() completes with no emissions when NacosDiscovery.getInstances() returns empty list
  @Test
  public void getInstancesCompletesEmptyWhenNoInstancesFound() throws NacosException {
    Mockito.when(nacosDiscovery.getInstances("empty-service")).thenReturn(Collections.emptyList());
    List<ServiceInstance> result = nacosReactiveDiscoveryClient.getInstances("empty-service")
        .collectList().block();

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getInstances("empty-service");
  }

  // Test scenario: getInstances() completes with no emissions when serviceId is null (Mono.justOrEmpty behavior)
  @Test
  public void getInstancesCompletesEmptyWhenServiceIdIsNull() throws NacosException {
    List<ServiceInstance> result = nacosReactiveDiscoveryClient.getInstances(null).collectList().block();

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    Mockito.verify(nacosDiscovery, Mockito.never()).getInstances(Mockito.anyString());
  }

  // Test scenario: getServices() emits service names when NacosDiscovery.getServices() succeeds
  @Test
  public void getServicesReturnsServicesWhenDiscoverySucceeds() throws NacosException {
    List<String> expectedServices = Arrays.asList("service-A", "service-B", "service-C");
    Mockito.when(nacosDiscovery.getServices()).thenReturn(expectedServices);
    List<String> result = nacosReactiveDiscoveryClient.getServices().collectList().block();

    Assertions.assertNotNull(result);
    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals("service-A", result.get(0));
    Assertions.assertEquals("service-B", result.get(1));
    Assertions.assertEquals("service-C", result.get(2));
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getServices();
  }

  // Test scenario: getServices() completes with no emissions when NacosDiscovery.getServices() throws Exception
  @Test
  public void getServicesCompletesEmptyWhenExceptionThrown() throws NacosException {
    NacosException nacosException = new NacosException(NacosException.SERVER_ERROR, "nacos server error");
    Mockito.when(nacosDiscovery.getServices()).thenThrow(nacosException);
    List<String> result = nacosReactiveDiscoveryClient.getServices().collectList().block();

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getServices();
  }

  // Test scenario: getServices() completes with no emissions when NacosDiscovery.getServices() returns empty list
  @Test
  public void getServicesCompletesEmptyWhenNoServicesRegistered() throws NacosException {
    Mockito.when(nacosDiscovery.getServices()).thenReturn(Collections.emptyList());
    List<String> result = nacosReactiveDiscoveryClient.getServices().collectList().block();

    Assertions.assertNotNull(result);
    Assertions.assertTrue(result.isEmpty());
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getServices();
  }
}