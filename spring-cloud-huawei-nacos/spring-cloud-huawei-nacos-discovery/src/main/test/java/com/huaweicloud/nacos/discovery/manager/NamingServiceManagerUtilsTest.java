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

package com.huaweicloud.nacos.discovery.manager;

import java.util.Properties;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NamingServiceManagerUtilsTest {
  @Test
  public void testBuildMasterServerProperties() {
    NacosDiscoveryProperties properties = buildNacosDiscoveryProperties();
    Properties pro = NamingServiceManagerUtils.buildMasterServerProperties(properties);
    Assertions.assertEquals("public", pro.getProperty("namespace"));
    Assertions.assertEquals("127.0.0.1", pro.getProperty("serverAddr"));
    Assertions.assertEquals("asan", pro.getProperty("username"));
    Assertions.assertEquals("123456", pro.getProperty("password"));
    Assertions.assertEquals("ak", pro.getProperty("accessKey"));
    Assertions.assertEquals("sk", pro.getProperty("secretKey"));
    Assertions.assertEquals("log", pro.getProperty("com.alibaba.nacos.naming.log.filename"));
    Assertions.assertEquals("default", pro.getProperty("clusterName"));
    Assertions.assertEquals("cache", pro.getProperty("namingLoadCacheAtStart"));
  }

  @Test
  public void testBuildStandbyServerProperties() {
    NacosDiscoveryProperties properties = buildNacosDiscoveryProperties();
    Properties pro = NamingServiceManagerUtils.buildStandbyServerProperties(properties);
    Assertions.assertEquals("public", pro.getProperty("namespace"));
    Assertions.assertEquals("196.168.168.1", pro.getProperty("serverAddr"));
    Assertions.assertEquals("asan", pro.getProperty("username"));
    Assertions.assertEquals("123456", pro.getProperty("password"));
    Assertions.assertEquals("ak", pro.getProperty("accessKey"));
    Assertions.assertEquals("sk", pro.getProperty("secretKey"));
    Assertions.assertEquals("log", pro.getProperty("com.alibaba.nacos.naming.log.filename"));
    Assertions.assertEquals("default", pro.getProperty("clusterName"));
    Assertions.assertEquals("cache", pro.getProperty("namingLoadCacheAtStart"));
  }

  @Test
  public void testBuildTaskScheduler() {
    ThreadPoolTaskScheduler task = NamingServiceManagerUtils.buildTaskScheduler("test");
    Assertions.assertTrue(task.isRunning());
  }

  private NacosDiscoveryProperties buildNacosDiscoveryProperties() {
    NacosDiscoveryProperties properties = new NacosDiscoveryProperties();
    properties.setServerAddr("127.0.0.1");
    properties.setStandbyServerAddr("196.168.168.1");
    properties.setNamespace("public");
    properties.setUsername("asan");
    properties.setPassword("123456");
    properties.setAccessKey("ak");
    properties.setSecretKey("sk");
    properties.setLogName("log");
    properties.setClusterName("default");
    properties.setNamingLoadCacheAtStart("cache");
    return properties;
  }
}
