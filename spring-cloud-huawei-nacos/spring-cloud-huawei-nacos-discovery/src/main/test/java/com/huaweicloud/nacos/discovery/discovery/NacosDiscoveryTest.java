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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.cloud.client.ServiceInstance;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.manager.NamingServiceManager;
import com.huaweicloud.nacos.discovery.manager.NamingServiceMasterManager;

public class NacosDiscoveryTest {
  private final NacosDiscoveryProperties discoveryProperties = new NacosDiscoveryProperties();

  private final List<NamingServiceManager> namingServiceManagers = new ArrayList<>();

  private final NacosCrossGroupProperties crossGroupProperties = new NacosCrossGroupProperties();

  @Test
  public void testGetInstances() throws NacosException {
    NamingServiceMasterManager manager = Mockito.mock(NamingServiceMasterManager.class);
    Mockito.when(manager.isNacosServerHealth()).thenReturn(true);
    namingServiceManagers.add(manager);
    NamingService namingService = Mockito.mock(NamingService.class);
    Mockito.when(manager.getNamingService()).thenReturn(namingService);
    List<Instance> list = new ArrayList<>();
    list.add(buildInstance());
    Mockito.when(namingService.selectInstances("test", "DEFAULT_GROUP", true))
        .thenReturn(list);
    NacosDiscovery discovery = new NacosDiscovery(discoveryProperties, namingServiceManagers, crossGroupProperties);
    List<ServiceInstance> instances = discovery.getInstances("test");
    Assertions.assertEquals(1, instances.size());
  }

  @Test
  public void testHostToServiceInstance() {
    NacosDiscovery discovery = new NacosDiscovery(discoveryProperties, namingServiceManagers, crossGroupProperties);
    Instance instance = buildInstance();
    ServiceInstance serviceInstance = discovery.hostToServiceInstance(instance, "test-id");
    Assertions.assertTrue("test-id".equals(serviceInstance.getServiceId())
        && "abc123".equals(serviceInstance.getInstanceId()));
  }

  private Instance buildInstance() {
    Instance instance = new Instance();
    instance.setIp("127.0.0.1");
    instance.setPort(8080);
    instance.setInstanceId("abc123");
    instance.setServiceName("test");
    instance.setWeight(100);
    instance.setClusterName("default");
    return instance;
  }
}
