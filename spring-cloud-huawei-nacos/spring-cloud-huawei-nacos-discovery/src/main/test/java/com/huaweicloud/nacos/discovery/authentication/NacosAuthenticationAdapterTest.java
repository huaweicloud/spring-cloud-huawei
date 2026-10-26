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

package com.huaweicloud.nacos.discovery.authentication;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.huaweicloud.nacos.discovery.NacosServiceInstance;

public class NacosAuthenticationAdapterTest {
  private final Registration registration = Mockito.mock(Registration.class);

  private final DiscoveryClient serviceDiscovery = Mockito.mock(DiscoveryClient.class);

  private final NacosAuthenticationAdapter adapter = new NacosAuthenticationAdapter(serviceDiscovery);

  @BeforeEach
  public void setUp() {
    Mockito.when(registration.getHost()).thenReturn("127.0.0.1");
    Mockito.when(registration.getPort()).thenReturn(8080);

    NacosServiceInstance serviceInstance = new NacosServiceInstance();
    serviceInstance.setServiceId("test");
    serviceInstance.setHost("127.0.0.1");
    serviceInstance.setInstanceId("abc123");
    serviceInstance.setPort(8080);
    Map<String, String> metadata = new HashMap<>();
    metadata.put("publickey", "123456");
    serviceInstance.setMetadata(metadata);
    List<ServiceInstance> list = new ArrayList<>();
    list.add(serviceInstance);
    Mockito.when(serviceDiscovery.getInstances("test")).thenReturn(list);
  }

  @Test
  public void testGetInstanceId() {
    String instanceId = adapter.getInstanceId(registration);
    Assertions.assertEquals("127-0-0-1-8080", instanceId);

    Mockito.when(registration.getInstanceId()).thenReturn("abc123");
    instanceId = adapter.getInstanceId(registration);
    Assertions.assertEquals("abc123", instanceId);
  }

  @Test
  public void testGetPublicKeyFromInstance() {
    String property = adapter.getPublicKeyFromInstance("abc123", "test");
    Assertions.assertEquals("123456", property);
  }
}
