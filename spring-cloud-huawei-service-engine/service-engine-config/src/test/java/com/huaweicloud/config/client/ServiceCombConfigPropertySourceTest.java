/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.config.client;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.huaweicloud.config.ServiceCombConfigPropertySource;

public class ServiceCombConfigPropertySourceTest {
  @Test
  public void getPropertyNames() {
    Map<String, Object> sources = new HashMap<>();
    sources.put("test", "tt");
    sources.put("test2", "tt");

    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(sources);

    String[] result = serviceCombConfigPropertySource.getPropertyNames();
    Assertions.assertEquals(result.length, 2);
  }

  @Test
  public void getProperty() {
    Map<String, Object> sources = new HashMap<>();
    sources.put("test", "tt");
    sources.put("test2", "tt");
    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(sources);
    Object result = serviceCombConfigPropertySource.getProperty("test");
    Assertions.assertEquals(result, "tt");
  }
}