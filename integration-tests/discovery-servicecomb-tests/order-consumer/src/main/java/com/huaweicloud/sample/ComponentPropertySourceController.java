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
package com.huaweicloud.sample;

import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/order/component")
public class ComponentPropertySourceController {
  private final Environment environment;

  public ComponentPropertySourceController(Environment environment) {
    this.environment = environment;
  }

  @GetMapping("/testComponentPropertySourceWork")
  public String testComponentPropertySourceWork() throws Exception {
    String result = environment.getProperty("test.value.default");
    check("default", result);
    result = environment.getProperty("test.value.componentOverride");
    check("orderComponentOverride", result);
    result = environment.getProperty("test.value.bootstrapOverride");
    check("orderBootstrapOverride", result);
    result = environment.getProperty("test.value.applicationOverride");
    check("orderApplicationOverride", result);

    return "success";
  }

  private void check(String expected, String actual) {
    if (expected.equals(actual)) {
      return;
    }
    throw new IllegalStateException();
  }
}
