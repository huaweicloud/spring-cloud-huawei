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

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import reactor.core.publisher.Mono;

@RestController
@RequestMapping(path = "govern")
public class GovernanceController {

  private final RestTemplate restTemplate;

  private final FeignService feignService;

  private final WebfluxService webfluxService;

  private int count = 0;

  private int gatewayIsolationCounter = 0;

  private int retryCounter = 0;

  private int retryCounterMore = 0;

  @Autowired
  public GovernanceController(RestTemplate restTemplate, FeignService feignService,
      WebfluxService webfluxService) {
    this.restTemplate = restTemplate;
    this.feignService = feignService;
    this.webfluxService = webfluxService;
  }

  @RequestMapping("/hello")
  public String hello() {
    return restTemplate.getForObject("http://price/hello", String.class);
  }

  @RequestMapping("/rateLimiting")
  public String rateLimiting() {
    return "rateLimiting";
  }

  @GetMapping("/identifierRateLimiting")
  public String identifierRateLimiting() {
    return "identifierRateLimiting";
  }

  @GetMapping("/identifierRateLimitingService")
  public String identifierRateLimitingService() {
    for (int i = 0; i <= 10; i++) {
      if (!"success".equals(
          restTemplate.getForObject("http://price/rate/identifierRateLimitingService", String.class))) {
        return "failed";
      }
    }
    return "success";
  }

  @RequestMapping("/faultInjectionRestTemplate")
  public String faultInjectionRestTemplate() {
    return restTemplate.getForObject("http://price/faultInjection", String.class);
  }

  @RequestMapping("/serviceFaultInjectionRestTemplate")
  public String serviceFaultInjectionRestTemplate() {
    return restTemplate.getForObject("http://price/serviceNameFaultInjection", String.class);
  }

  @RequestMapping("/faultInjectionFeign")
  public String faultInjectionFeign() {
    return feignService.faultInjection();
  }

  @RequestMapping("/serviceNameFaultInjectionFeign")
  public String serviceNameFaultInjectionFeign() {
    return feignService.serviceNameFaultInjection();
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

  @RequestMapping("/retryMore")
  public String retryMore(@RequestParam(name = "invocationID") String invocationID) {
    return restTemplate.getForObject("http://price/retryMore?invocationID={1}", String.class, invocationID);
  }

  @RequestMapping("/serviceNameRetry")
  public String serviceNameRetry(@RequestParam(name = "invocationID") String invocationID) {
    return restTemplate.getForObject("http://price/serviceNameRetry?invocationID={1}", String.class, invocationID);
  }

  @RequestMapping("/retryFeign")
  public String retryFeign(@RequestParam(name = "invocationID") String invocationID) {
    return feignService.retry(invocationID);
  }

  @RequestMapping("/retryFeignMore")
  public String retryFeignMore(@RequestParam(name = "invocationID") String invocationID) {
    return feignService.retryMore(invocationID);
  }

  @RequestMapping("/serviceNameRetryFeign")
  public String serviceNameRetryFeign(@RequestParam(name = "invocationID") String invocationID) {
    return feignService.serviceNameRetry(invocationID);
  }

  @GetMapping(
      path = "/gatewayRetry",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<String>> gatewayRetry() {
    ResponseEntity<String> result;
    if (retryCounter % 2 == 0) {
      result = ResponseEntity.status(503).body("fail");
    } else {
      result = ResponseEntity.status(200).body("ok");
    }
    retryCounter++;
    return Mono.just(result);
  }

  @GetMapping(
      path = "/gatewayRetryMore",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<String>> gatewayRetryMore() {
    ResponseEntity<String> result;
    if (retryCounterMore % 5 != 4) {
      result = ResponseEntity.status(503).body("fail");
    } else {
      result = ResponseEntity.status(200).body("ok");
    }
    retryCounterMore++;
    return Mono.just(result);
  }

  @RequestMapping("/circuitBreaker")
  public String circuitBreaker() throws Exception {
    count++;
    if (count % 3 == 0) {
      return "ok";
    }
    throw new RuntimeException("test error");
  }

  @RequestMapping("/circuitBreakerHeader")
  public String circuitBreakerHeader(HttpServletResponse response) {
    response.addHeader("X-HTTP-STATUS-CODE", "502");
    return "success";
  }

  @RequestMapping("/bulkhead")
  public String bulkhead() {
    return restTemplate.getForObject("http://price/hello", String.class);
  }

  @RequestMapping("/isolationForceOpen")
  public String isolationForceOpen() {
    return restTemplate.getForObject("http://price/isolationForceOpen", String.class);
  }

  @RequestMapping("/testIsolationResponseHeader")
  public String testIsolationResponseHeader() {
    return restTemplate.getForObject("http://price/testIsolationResponseHeader", String.class);
  }

  @RequestMapping("/isolationForceOpenFeign")
  public String isolationForceOpenFeign() {
    return feignService.isolationForceOpen();
  }

  @RequestMapping("/testIsolationResponseHeaderFeign")
  public String testIsolationResponseHeaderFeign() {
    return feignService.testIsolationResponseHeader();
  }

  @RequestMapping("/gatewayIsolationForceOpenFeign")
  public String gatewayIsolationForceOpenFeign() {
    return "success";
  }

  @RequestMapping("/testGatewayIsolationErrorCode")
  public ResponseEntity<String> testGatewayIsolationErrorCode() {
    gatewayIsolationCounter++;
    if (gatewayIsolationCounter % 3 != 0) {
      return ResponseEntity.status(200).body("ok");
    }
    return ResponseEntity.status(503).body("fail");
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

  @RequestMapping("/gatewayInstanceBulkhead")
  public String gatewayInstanceBulkhead() throws Exception {
    Thread.sleep(500);
    return "gatewayInstanceBulkhead";
  }

  @RequestMapping("/feignInstanceBulkhead")
  public String feignInstanceBulkhead() throws Exception {
    return feignService.feignInstanceBulkhead();
  }

  @RequestMapping("/restTemplateInstanceBulkhead")
  public String restTemplateInstanceBulkhead() throws Exception {
    return restTemplate.getForObject("http://price/restTemplateInstanceBulkhead", String.class);
  }

  @RequestMapping("/loadbalance")
  public String loadbalance() {
    return feignService.loadbalabce();
  }

  @GetMapping("/testFeignFaultInjection")
  public String testFeignFaultInjection(@RequestParam String name) {
    webfluxService.sayHello(name);
    return feignService.testFaultInjection();
  }

  @GetMapping("/testTemplateFaultInjection")
  public String testTemplateFaultInjection(@RequestParam String name) {
    restTemplate.getForObject("http://webflux/sayHello?name=tom", String.class);
    return restTemplate.getForObject("http://price/faultInjection", String.class);
  }
}
