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

import com.huaweicloud.config.ServiceCombConfigProperties;
import com.huaweicloud.config.ServiceCombConfigPropertySource;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.config.client.ServiceCombConfigClient;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

/**
 * @Author wangqijun
 * @Date 17:43 2019-10-26
 **/
@RunWith(JMockit.class)
public class ServiceCombConfigPropertySourceTest extends MockUp<ServiceCombConfigPropertySource> {

  private Map<String, Object> properties = new HashMap<>();

  @Test
  public void loadAllRemoteConfig(@Injectable String name, @Injectable ServiceCombConfigClient source,
      @Injectable ServiceCombConfigProperties serviceCombConfigProperties)
      throws RemoteOperationException {
    name = "dd";
    Map<String, String> map = new HashMap<>();
    map.put("r", "r");
    map.put("d", "r");
    new Expectations() {
      {
        source.loadAll((ServiceCombConfigProperties) any, anyString);
        result = map;
      }
    };
    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(name, source);
    Deencapsulation.setField(serviceCombConfigPropertySource, properties);
    Map<String, String> result = serviceCombConfigPropertySource.loadAllRemoteConfig(serviceCombConfigProperties, "");
    Assert.assertEquals(result.size(), 2);
  }

  @Test
  public void getPropertyNames(@Injectable String name, @Injectable ServiceCombConfigClient source) {
    name = "dd";
    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(name, source);
    Deencapsulation.setField(serviceCombConfigPropertySource, properties);
    properties.put("test", "tt");
    properties.put("test2", "tt");
    String[] result = serviceCombConfigPropertySource.getPropertyNames();
    Assert.assertEquals(result.length, 2);
  }

  @Test
  public void getProperty(@Injectable String name, @Injectable ServiceCombConfigClient source) {
    name = "dd";
    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(name, source);
    Deencapsulation.setField(serviceCombConfigPropertySource, properties);
    properties.put("test", "tt");
    Object result = serviceCombConfigPropertySource.getProperty("test");
    Assert.assertEquals(result, "tt");
  }
}