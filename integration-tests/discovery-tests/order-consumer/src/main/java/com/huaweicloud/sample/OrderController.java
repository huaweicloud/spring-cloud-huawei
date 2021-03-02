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
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderController {

  @Autowired
  private DiscoveryClient discoveryClient;

  @Autowired
  private RestTemplate restTemplate;

  @RequestMapping("/instances")
  public Object instances() {
    return discoveryClient.getInstances("price");
  }

  @RequestMapping("/order")
  public String getOrder(@RequestParam("id") String id) {
    String callServiceResult = restTemplate.getForObject("http://price/price?id=" + id, String.class);
    return callServiceResult;
  }

  @RequestMapping("/configuration")
  public String getEnums() {
    return restTemplate.getForObject("http://price/configuration", String.class);
  }

  @RequestMapping(value = "/services", method = RequestMethod.GET)
  public Object services() {
    return discoveryClient.getServices();
  }
}
