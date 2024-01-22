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
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import com.huaweicloud.config.ServiceCombConfigPropertySource;
import com.huaweicloud.config.ServiceCombPropertySourceLocator;

@ExtendWith(MockitoExtension.class)
public class ServiceCombPropertySourceLocatorTest {

  @Mock
  Environment environment;

  @Test
  public void locate() {
    Map<String, Object> sources = new HashMap<>();
    sources.put("test", "tt");
    sources.put("test2", "tt");

    ServiceCombPropertySourceLocator serviceCombPropertySourceLocator = new ServiceCombPropertySourceLocator(sources);
    PropertySource<?> result = serviceCombPropertySourceLocator.locate(environment);
    Assertions.assertEquals(result.getName(), ServiceCombConfigPropertySource.NAME);
  }
}
