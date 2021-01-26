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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

/**
 * @Author GuoYl123
 * @Date 2020/4/22
 **/
@RestController
public class ConsumerController {

  @Autowired
  private RestTemplate restTemplate;

  private int count = 0;

  @RequestMapping("/hello")
  public String hello() {
    return restTemplate.getForObject("http://provider/hello", String.class);
  }

  @RequestMapping("/retry")
  public String retry() {
    return restTemplate.getForObject("http://provider/retry", String.class);
  }

  @RequestMapping("/circuitBreaker")
  public String circuitBreaker() throws Exception {
    count++;
    if (count % 3 == 0) {
      return "ok";
    }
    throw new RuntimeException("test error");
  }

  @RequestMapping("/bulkhead")
  public String bulkhead() {
    return restTemplate.getForObject("http://provider/hello", String.class);
  }
}
