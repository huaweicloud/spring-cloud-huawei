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

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

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

  @GetMapping("/testContextMapper")
  public String testContextMapper(@RequestParam("query-context") String queryContext) {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    String result = context.getContext("x-query-context");
    result += context.getContext("x-header-context");
    result += queryContext;
    return result;
  }

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

  @RequestMapping("/invocationContext")
  public String invocationContext() {
    InvocationContext invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    if (!"test01".equals(invocationContext.getContext("test01"))) {
      return null;
    }
    invocationContext.putContext("test02", "test02");
    return restTemplate.getForObject("http://price/invocationContext", String.class);
  }

  @RequestMapping("/invocationContextGateway")
  public String invocationContextGateway() {
    InvocationContext invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    if (!"test01".equals(invocationContext.getContext("test01"))) {
      return null;
    }
    if (!"test03".equals(invocationContext.getContext("test03"))) {
      return null;
    }
    if (!"discovery-gateway".equals(invocationContext.getContext(InvocationContext.CONTEXT_MICROSERVICE_NAME))) {
      return null;
    }
    if (StringUtils.isEmpty(invocationContext.getContext(InvocationContext.CONTEXT_INSTANCE_ID))) {
      return null;
    }
    return "success";
  }

  @RequestMapping("/invocationContextFeign")
  public String invocationContextFeign() {
    InvocationContext invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    if (!"test01".equals(invocationContext.getContext("test01"))) {
      return null;
    }
    invocationContext.putContext("test02", "test02");
    return feignService.invocationContext();
  }

  @RequestMapping(value = "/services", method = RequestMethod.GET)
  public Object services() {
    return discoveryClient.getServices();
  }


  @RequestMapping("/orderBal")
  public String orderBal(@RequestParam("id") String id) {
    String callServiceResult = restTemplate.getForObject("http://price/priceBalance?id=" + id, String.class);
    return callServiceResult;
  }

  @GetMapping(
      path = "/testWebClient",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public String testWebClient() {
    return "ok";
  }

  @PostMapping("/testPostModel")
  public PojoModel testPostModel(@RequestBody PojoModel model) {
    return restTemplate.postForObject("http://price/testPostModel", model, PojoModel.class);
  }

  @PostMapping("/testPostModelFeign")
  public PojoModel testPostModelFeign(@RequestBody PojoModel model) {
    return feignService.testPostModel(model);
  }

  @PostMapping("/testHeaderWithJsonWrong")
  public String testHeaderWithJsonWrong(@RequestHeader String model) {
    return feignService.testHeaderWithJsonWrong(model);
  }

  @PostMapping("/testHeaderWithJsonCorrect")
  public String testHeaderWithJsonCorrect(@RequestHeader String model) {
    return feignService.testHeaderWithJsonCorrect(model);
  }
}
