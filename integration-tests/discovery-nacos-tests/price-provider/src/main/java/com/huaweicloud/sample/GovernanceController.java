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

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GovernanceController {
  private final Map<String, Integer> retryTimes = new HashMap<>();

  @RequestMapping("/hello")
  public String sayHello() {
    return "Hello world!";
  }

  @RequestMapping("/faultInjection")
  public String faultInjection() {
    return "success";
  }

  @RequestMapping("/routeFaultInjectionNull")
  public String routeFaultInjectionNull() {
    return "success";
  }

  @RequestMapping("/serviceNameFaultInjection")
  public String serviceNameFaultInjection() {
    return "success";
  }

  @RequestMapping("/faultInjectionModel")
  public PojoModel faultInjectionModel() {
    return new PojoModel(2, "hello");
  }

  @RequestMapping("/retry")
  public String retry(HttpServletResponse response, @RequestParam(name = "invocationID") String invocationID) {
    retryTimes.putIfAbsent(invocationID, 0);
    retryTimes.put(invocationID, retryTimes.get(invocationID) + 1);

    int retry = retryTimes.get(invocationID);

    if (retry == 3) {
      return "try times: " + retry;
    }
    response.setStatus(502);
    return "failed result";
  }

  @RequestMapping("/retryMore")
  public String retryMore(HttpServletResponse response, @RequestParam(name = "invocationID") String invocationID) {
    retryTimes.putIfAbsent(invocationID, 0);
    retryTimes.put(invocationID, retryTimes.get(invocationID) + 1);

    int retry = retryTimes.get(invocationID);

    if (retry == 6) {
      return "try times: " + retry;
    }
    response.setStatus(503);
    return "failed result";
  }

  @RequestMapping("/serviceNameRetry")
  public String serviceNameRetry(HttpServletResponse response, @RequestParam(name = "invocationID") String invocationID) {
    retryTimes.putIfAbsent(invocationID, 0);
    retryTimes.put(invocationID, retryTimes.get(invocationID) + 1);

    int retry = retryTimes.get(invocationID);

    if (retry == 3) {
      return "try times: " + retry;
    }
    response.setStatus(502);
    return "failed result";
  }

  @RequestMapping("/gateway/retry")
  public String gatewayRetry(HttpServletResponse response, @RequestParam(name = "invocationID") String invocationID) {
    retryTimes.putIfAbsent(invocationID, 0);
    retryTimes.put(invocationID, retryTimes.get(invocationID) + 1);

    int retry = retryTimes.get(invocationID);

    if (retry == 3) {
      return "try times: " + retry;
    }
    response.setStatus(502);
    return "failed result";
  }

  @RequestMapping("/circuitBreaker")
  public String circuitBreaker() {
    throw new RuntimeException("circuitBreaker by provider.");
  }

  @RequestMapping("/isolationForceOpen")
  public String isolationForceOpen() {
    return "success";
  }

  @GetMapping("/rate/testRateLimitForService")
  public String testRateLimitForService() {
    return "success";
  }

  @GetMapping("/rate/identifierRateLimitingService")
  public String identifierRateLimitingService() {
    return "success";
  }

  @GetMapping("/feignInstanceBulkhead")
  public String feignInstanceBulkhead() throws Exception {
    Thread.sleep(500);
    return "success";
  }

  @GetMapping("/restTemplateInstanceBulkhead")
  public String restTemplateInstanceBulkhead() throws Exception {
    Thread.sleep(500);
    return "success";
  }

  @RequestMapping("/loadbalance")
  public String loadbalance() {
    return "I am price";
  }
}
