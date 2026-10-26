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

package com.huaweicloud.nacos.discovery.discovery;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.alibaba.nacos.api.exception.NacosException;
import com.huaweicloud.nacos.discovery.NacosServiceInstance;

import org.springframework.cloud.client.ServiceInstance;

public class NacosDiscoveryClientTest {

  private NacosDiscovery nacosDiscovery;

  private NacosDiscoveryClient nacosDiscoveryClient;

  @BeforeEach
  public void setUp() {
    nacosDiscovery = Mockito.mock(NacosDiscovery.class);
    nacosDiscoveryClient = new NacosDiscoveryClient(nacosDiscovery);
  }

  // Test scenario: description() returns the predefined constant string
  @Test
  public void descriptionReturnsConstantDescription() {
    String result = nacosDiscoveryClient.description();

    Assertions.assertEquals(NacosDiscoveryClient.DESCRIPTION, result);
    Assertions.assertEquals("Nacos Discovery Client", result);
  }

  // Test scenario: getInstances() returns service instances when NacosDiscovery succeeds
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

    List<ServiceInstance> result = nacosDiscoveryClient.getInstances("service-A");

    Assertions.assertEquals(2, result.size());
    Assertions.assertEquals("service-A", result.get(0).getServiceId());
    Assertions.assertEquals("10.0.0.1", result.get(0).getHost());
    Assertions.assertEquals(8080, result.get(0).getPort());
    Assertions.assertEquals("service-A", result.get(1).getServiceId());
    Assertions.assertEquals("10.0.0.2", result.get(1).getHost());
    Assertions.assertEquals(8081, result.get(1).getPort());
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getInstances("service-A");
  }

  // Test scenario: getInstances() returns empty list when no instances are found
  @Test
  public void getInstancesReturnsEmptyListWhenNoInstancesFound() throws NacosException {
    Mockito.when(nacosDiscovery.getInstances("nonexistent-service")).thenReturn(Collections.emptyList());

    List<ServiceInstance> result = nacosDiscoveryClient.getInstances("nonexistent-service");

    Assertions.assertTrue(result.isEmpty());
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getInstances("nonexistent-service");
  }

  // Test scenario: getInstances() wraps NacosException into RuntimeException with correct message and cause
  @Test
  public void getInstancesThrowsRuntimeExceptionWhenDiscoveryFails() throws NacosException {
    NacosException nacosException = new NacosException(NacosException.SERVER_ERROR, "nacos server error");
    Mockito.when(nacosDiscovery.getInstances("failing-service")).thenThrow(nacosException);

    RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
        () -> nacosDiscoveryClient.getInstances("failing-service"));

    Assertions.assertTrue(thrown.getMessage().contains("failing-service"));
    Assertions.assertTrue(thrown.getMessage().contains("find services from nacos server failed"));
    Assertions.assertSame(nacosException, thrown.getCause());
  }

  // Test scenario: getServices() returns service names when NacosDiscovery succeeds
  @Test
  public void getServicesReturnsServiceNamesWhenDiscoverySucceeds() throws NacosException {
    List<String> expectedServices = Arrays.asList("service-A", "service-B", "service-C");
    Mockito.when(nacosDiscovery.getServices()).thenReturn(expectedServices);

    List<String> result = nacosDiscoveryClient.getServices();

    Assertions.assertEquals(3, result.size());
    Assertions.assertEquals("service-A", result.get(0));
    Assertions.assertEquals("service-B", result.get(1));
    Assertions.assertEquals("service-C", result.get(2));
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getServices();
  }

  // Test scenario: getServices() returns empty list when no services are registered
  @Test
  public void getServicesReturnsEmptyListWhenNoServicesRegistered() throws NacosException {
    Mockito.when(nacosDiscovery.getServices()).thenReturn(Collections.emptyList());

    List<String> result = nacosDiscoveryClient.getServices();

    Assertions.assertTrue(result.isEmpty());
    Mockito.verify(nacosDiscovery, Mockito.times(1)).getServices();
  }

  // Test scenario: getServices() wraps NacosException into RuntimeException with correct message and cause
  @Test
  public void getServicesThrowsRuntimeExceptionWhenDiscoveryFails() throws NacosException {
    NacosException nacosException = new NacosException(NacosException.SERVER_ERROR, "nacos server error");
    Mockito.when(nacosDiscovery.getServices()).thenThrow(nacosException);

    RuntimeException thrown = Assertions.assertThrows(RuntimeException.class,
        () -> nacosDiscoveryClient.getServices());

    Assertions.assertTrue(thrown.getMessage().contains("get service names from nacos server failed"));
    Assertions.assertSame(nacosException, thrown.getCause());
  }
}
