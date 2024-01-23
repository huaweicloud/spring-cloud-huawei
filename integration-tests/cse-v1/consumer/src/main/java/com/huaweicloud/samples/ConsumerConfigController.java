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

package com.huaweicloud.samples;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ConsumerConfigController {

  private final Environment environment;

  private final ConsumerConfigurationProperties consumerConfigurationProperties;

  @Autowired
  public ConsumerConfigController(Environment environment, ConsumerConfigurationProperties consumerConfigurationProperties) {
    this.environment = environment;
    this.consumerConfigurationProperties = consumerConfigurationProperties;
  }

  @GetMapping("/config")
  public String config(@RequestParam("key") String key) {
    return environment.getProperty(key);
  }

  @GetMapping("/foo")
  public String foo() {
    return consumerConfigurationProperties.getFoo();
  }

  @GetMapping("/bar")
  public String bar() {
    return consumerConfigurationProperties.getBar();
  }

  @GetMapping("/sequences")
  public List<String> sequences() {
    return consumerConfigurationProperties.getSequences();
  }

  @GetMapping("/models")
  public List<ConfigModel> models() {
    return consumerConfigurationProperties.getConfigModels();
  }
}
