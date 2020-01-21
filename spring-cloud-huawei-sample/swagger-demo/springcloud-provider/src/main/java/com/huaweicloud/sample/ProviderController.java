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
package com.huaweicloud.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author GuoYl123
 * @Date 2019/12/16
 **/
@RestController
public class ProviderController {

  @Autowired
  RestTemplate restTemplate;

  @GetMapping("/foo")
  public Foo foo(@RequestParam("id") int id) {
    return new Foo("foo", id, null);
  }

  @GetMapping("/hello")
  public String sayHello(@RequestParam("name") String name) {
    return "spring cloud hello world " + name;
  }

  @GetMapping("/int")
  public int intTest() {
    return 123;
  }

  @GetMapping("/invoke")
  public String invoke() {
    return restTemplate
        .getForObject("http://swagger-consumer/consumer/invoke",String.class);
  }
}
