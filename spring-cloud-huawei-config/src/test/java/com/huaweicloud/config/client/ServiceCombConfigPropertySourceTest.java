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

package com.huaweicloud.config.client;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.config.common.ConfigConverter;
import org.junit.Assert;
import org.junit.Test;

import com.huaweicloud.config.ServiceCombConfigPropertySource;

import mockit.MockUp;

public class ServiceCombConfigPropertySourceTest extends MockUp<ServiceCombConfigPropertySource> {

  private Map<String, Object> properties = new HashMap<>();

  @Test
  public void getPropertyNames() {
    Map<String, Object> sources = new HashMap<>();
    sources.put("test", "tt");
    sources.put("test2", "tt");
    ConfigConverter configConverter = new ConfigConverter(null);
    configConverter.updateData(sources);

    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(
        configConverter);

    String[] result = serviceCombConfigPropertySource.getPropertyNames();
    Assert.assertEquals(result.length, 2);
  }

  @Test
  public void getProperty() {
    Map<String, Object> sources = new HashMap<>();
    sources.put("test", "tt");
    sources.put("test2", "tt");
    ConfigConverter configConverter = new ConfigConverter(null);
    configConverter.updateData(sources);

    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(
        configConverter);

    Object result = serviceCombConfigPropertySource.getProperty("test");
    Assert.assertEquals(result, "tt");
  }
}