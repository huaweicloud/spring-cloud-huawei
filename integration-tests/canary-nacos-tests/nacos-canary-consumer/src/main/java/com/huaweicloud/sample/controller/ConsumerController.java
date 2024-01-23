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
package com.huaweicloud.sample.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class ConsumerController {

  private final RestTemplate restTemplate;

  @Autowired
  public ConsumerController(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }

  @GetMapping("/sayHelloCanary")
  public String sayHelloCanary(@RequestParam("name") String name) {
    MultiValueMap<String, String> headers = new HttpHeaders();
    headers.add("canary","old");
    HttpEntity<Object> entity = new HttpEntity<>(headers);
    String result = restTemplate
        .exchange("http://canary-provider/sayHelloCanary?name=World", HttpMethod.GET, entity, String.class).getBody();
    // 组合请求头与请求体参数
    return result;
  }
}
