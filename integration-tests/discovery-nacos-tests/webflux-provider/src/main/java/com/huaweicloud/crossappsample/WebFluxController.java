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
package com.huaweicloud.crossappsample;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

@RestController
public class WebFluxController {
  private int circuitBreakerCounter = 0;

  private final Map<String, Integer> retryTimes = new HashMap<>();

  private AtomicLong isolationCounter = new AtomicLong(0);

  @RequestMapping("/sayHello")
  public Mono<String> sayHello(@RequestParam("name") String name) {
    return Mono.just(name);
  }

  @RequestMapping("/testWebFluxInvocationContext")
  public Mono<String> testWebFluxInvocationContext(ServerWebExchange exchange, @RequestParam("name") String name) {
    InvocationContext context = exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY);
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    sb.append(".");
    sb.append(context.getContext("x-c"));
    sb.append(".");
    sb.append(context.getContext("x-header-context"));
    sb.append(".");
    sb.append(context.getContext("x-u"));
    sb.append(".");
    sb.append(context.getContext("x-m"));
    return Mono.just(sb.toString());
  }

  @GetMapping(
      path = "/testWebFluxServiceRateLimiting",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebFluxServiceRateLimiting() {
    return Mono.just("OK");
  }

  @GetMapping(
      path = "/testWebFluxServiceIdentifierRateLimiting",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebFluxServiceIdentifierRateLimiting() {
    return Mono.just("OK");
  }

  @GetMapping(
      path = "/testWebFluxServiceCircuitBreaker",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebFluxServiceCircuitBreaker() {
    circuitBreakerCounter++;
    if (circuitBreakerCounter % 3 != 0) {
      return Mono.just("OK");
    }
    throw new RuntimeException("test error");
  }

  @GetMapping(
      path = "/testWebFluxServiceBulkhead",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebFluxServiceBulkhead() {
    return Mono.delay(Duration.ofMillis(500)).then(Mono.just("OK"));
  }

  @GetMapping(
      path = "/testWebClientRetry",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<String>> testWebClientRetry(@RequestParam(name = "invocationID") String invocationID) {
    retryTimes.putIfAbsent(invocationID, 0);
    retryTimes.put(invocationID, retryTimes.get(invocationID) + 1);

    int retry = retryTimes.get(invocationID);

    if (retry == 3) {
      return Mono.just(ResponseEntity.status(200).body("try times: " + retry));
    }
    return Mono.just(ResponseEntity.status(503).body("fail"));
  }

  @GetMapping(
      path = "/testWebClientBulkhead",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebClientBulkhead() {
    return Mono.delay(Duration.ofMillis(500)).then(Mono.just("OK"));
  }

  @GetMapping(
      path = "/testWebClientInstanceIsolation",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<String>> testWebClientInstanceIsolation(
      @RequestParam(name = "invocationID") String invocationID) {
    if (isolationCounter.getAndIncrement() % 3 != 0) {
      return Mono.just(ResponseEntity.status(200).body("ok"));
    }
    return Mono.just(ResponseEntity.status(503).body("fail"));
  }
}
