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

package com.huaweicloud.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.cloud.bootstrap.config.PropertySourceLocator;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.CompositePropertySource;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

@Order(0)
public class ServiceCombPropertySourceLocator implements PropertySourceLocator {
  private final Map<String, Object> data;

  // Spring cloud context will create new ServiceCombPropertySourceLocator
  // if configuration changed. Make a copy of this data so spring cloud context
  // is able to detect config change.
  public ServiceCombPropertySourceLocator(Map<String, Object> data) {
    this.data = new HashMap<>();
    this.data.putAll(data);
  }

  @Override
  public PropertySource<?> locate(Environment environment) {
    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(this.data);
    CompositePropertySource composite = new CompositePropertySource(ServiceCombConfigPropertySource.NAME);
    composite.addPropertySource(serviceCombConfigPropertySource);
    return composite;
  }
}
