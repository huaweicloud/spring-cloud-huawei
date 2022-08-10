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

package com.huaweicloud.sample;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping(path = "govern")
public class GovernanceController {

  private final RestTemplate restTemplate;

  private final FeignService feignService;

  private int count = 0;

  @Autowired
  public GovernanceController(RestTemplate restTemplate, FeignService feignService) {
    this.restTemplate = restTemplate;
    this.feignService = feignService;
  }

  @RequestMapping("/hello")
  public String hello() {
    return restTemplate.getForObject("http://price/hello", String.class);
  }

  @RequestMapping("/rateLimiting")
  public String rateLimiting() {
    return "rateLimiting";
  }

  @RequestMapping("/faultInjectionRestTemplate")
  public String faultInjectionRestTemplate() {
    return restTemplate.getForObject("http://price/faultInjection", String.class);
  }

  @RequestMapping("/faultInjectionFeign")
  public String faultInjectionFeign() {
    return feignService.faultInjection();
  }

  @RequestMapping("/faultInjectionRestTemplateModel")
  public PojoModel faultInjectionRestTemplateModel() {
    return restTemplate.getForObject("http://price/faultInjectionModel", PojoModel.class);
  }

  @RequestMapping("/faultInjectionFeignModel")
  public PojoModel faultInjectionFeignModel() {
    return feignService.faultInjectionModel();
  }

  @RequestMapping("/retry")
  public String retry(@RequestParam(name = "invocationID") String invocationID) {
    return restTemplate.getForObject("http://price/retry?invocationID={1}", String.class, invocationID);
  }

  @RequestMapping("/retryFeign")
  public String retryFeign(@RequestParam(name = "invocationID") String invocationID) {
    return feignService.retry(invocationID);
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
    return restTemplate.getForObject("http://price/hello", String.class);
  }

  @RequestMapping("/isolationForceOpen")
  public String isolationForceOpen() {
    return restTemplate.getForObject("http://price/isolationForceOpen", String.class);
  }

  @RequestMapping("/isolationForceOpenFeign")
  public String isolationForceOpenFeign() {
    return feignService.isolationForceOpen();
  }

  @RequestMapping("/gatewayIsolationForceOpenFeign")
  public String gatewayIsolationForceOpenFeign() {
    return "success";
  }

  @GetMapping("/rate/testRateLimitForService")
  public String testRateLimitForService() {
    for (int i = 0; i <= 10; i++) {
      if (!"success".equals(restTemplate.getForObject("http://price/rate/testRateLimitForService", String.class))) {
        return "failed";
      }
    }
    return "success";
  }
}
