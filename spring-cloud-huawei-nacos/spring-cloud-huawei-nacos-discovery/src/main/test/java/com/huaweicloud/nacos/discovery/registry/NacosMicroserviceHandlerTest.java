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

package com.huaweicloud.nacos.discovery.registry;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.naming.pojo.Instance;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NacosMicroserviceHandlerTest {
  private final Environment environment = Mockito.mock(Environment.class);

  @Test
  public void testParseArrayValue() {
    List<String> list = NacosMicroserviceHandler.parseArrayValue("name:asan,password:12312,test:123");
    Assertions.assertEquals(3, list.size());
  }

  @Test
  public void testCreateMicroserviceMetadata() {
    String serviceProps = "name:asan,password:12312,test:123";
    Mockito.when(environment.getProperty("SERVICECOMB_SERVICE_PROPS")).thenReturn(serviceProps);
    Map<String, String> map = NacosMicroserviceHandler.createMicroserviceMetadata(environment);
    Assertions.assertEquals(3, map.size());
  }

  @Test
  public void testCreateMicroserviceInstance() {
    Mockito.when(environment.getProperty("CAS_APPLICATION_ID")).thenReturn("basic");
    Mockito.when(environment.getProperty("CAS_COMPONENT_NAME")).thenReturn("component");
    Mockito.when(environment.getProperty("CAS_INSTANCE_VERSION")).thenReturn("0.0.1");
    Mockito.when(environment.getProperty("CAS_INSTANCE_ID")).thenReturn("abc123");
    Mockito.when(environment.getProperty("CAS_ENVIRONMENT_ID")).thenReturn("env");
    Mockito.when(environment.getProperty("VERSION_MAPPING")).thenReturn("mapping");
    Mockito.when(environment.getProperty("SERVICECOMB_INSTANCE_PROPS")).thenReturn("name:asan,password:12312,test:123");
    Instance instance = NacosMicroserviceHandler.createMicroserviceInstance(buildNacosDiscoveryProperties(), environment);
    Assertions.assertEquals(13, instance.getMetadata().size());
    Assertions.assertEquals("127.0.0.1", instance.getIp());
    Assertions.assertEquals("default", instance.getClusterName());
    Assertions.assertEquals(8080, instance.getPort());
  }

  private NacosDiscoveryProperties buildNacosDiscoveryProperties() {
    NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
    properties.setWeight(100);
    properties.setInstanceEnabled(true);
    properties.setClusterName("default");
    properties.setEphemeral(false);
    properties.setIp("127.0.0.1");
    properties.setPort(8080);
    properties.setSecure(true);
    properties.setHeartBeatInterval(15000);
    properties.setIpDeleteTimeout(15000);
    properties.setHeartBeatTimeout(15000);
    return properties;
  }
}
