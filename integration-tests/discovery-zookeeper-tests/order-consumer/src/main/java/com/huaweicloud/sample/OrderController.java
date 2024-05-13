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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class OrderController {

  private final DiscoveryClient discoveryClient;

  private final RestTemplate restTemplate;

  private final FeignService feignService;

  @Autowired
  public OrderController(DiscoveryClient discoveryClient, RestTemplate restTemplate, FeignService feignService) {
    this.discoveryClient = discoveryClient;
    this.restTemplate = restTemplate;
    this.feignService = feignService;
  }

  @RequestMapping("/instances")
  public Object instances() {
    return discoveryClient.getInstances("price");
  }

  @RequestMapping("/order")
  public String getOrder(@RequestParam("id") String id) {
    return restTemplate.getForObject("http://price/price?id=" + id, String.class);
  }

  @RequestMapping("/orderAlias")
  public String getOrderAlias(@RequestParam("id") String id) {
    return restTemplate.getForObject("http://alias-price/price?id=" + id, String.class);
  }

  @RequestMapping("/orderFeign")
  public String getOrderFeign(@RequestParam("id") String id) {
    return feignService.getPriceFeign(id);
  }
}
