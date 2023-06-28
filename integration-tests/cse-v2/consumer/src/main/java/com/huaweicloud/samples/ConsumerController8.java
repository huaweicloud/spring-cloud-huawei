/*

  * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController8 {

  private final RestTemplate restTemplate;

  @Autowired
  public ConsumerController8(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  // consumer service which delegate the implementation to provider service.
  @GetMapping("/sayHello8")
  public String sayHello(@RequestParam("name") String name) {
    return restTemplate.getForObject("http://basic-provider/sayHello?name={1}", String.class, name);
  }
  @GetMapping("/jasypt18")
  public String jasypt1() {
    return restTemplate.getForObject("http://basic-provider/jasypt1", String.class);
  }
  @GetMapping("/jasypt28")
  public String jasypt2() {
    return restTemplate.getForObject("http://basic-provider/jasypt2", String.class);
  }
}
