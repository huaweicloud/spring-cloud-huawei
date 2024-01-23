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

package com.huaweicloud.samples;

import java.time.Duration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

@RestController
public class GatewayController {
  private int circuitBreakerCounter = 0;

  private int circuitBreakerErrorCodeCounter = 0;

  private WebClient.Builder webClientBuilder;

  @Autowired
  public GatewayController(WebClient.Builder webClientBuilder) {
    this.webClientBuilder = webClientBuilder;
  }

  @GetMapping(
      path = "/identifierRateLimiting",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> identifierRateLimiting() {
    return Mono.just("OK");
  }

  @GetMapping(
      path = "/testCircuitBreaker",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testCircuitBreaker() {
    circuitBreakerCounter++;
    if (circuitBreakerCounter % 3 != 0) {
      return Mono.just("ok");
    }
    throw new RuntimeException("test error");
  }

  @GetMapping(
      path = "/testCircuitBreakerErrorCode",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<ResponseEntity<String>> testCircuitBreakerErrorCode() {
    circuitBreakerErrorCodeCounter++;
    if (circuitBreakerErrorCodeCounter % 3 != 0) {
      return Mono.just(ResponseEntity.status(200).body("ok"));
    }
    return Mono.just(ResponseEntity.status(503).body("fail"));
  }

  @GetMapping(
      path = "/testBulkhead",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testBulkhead() {
    return Mono.delay(Duration.ofMillis(500)).then(Mono.just("ok"));
  }

  @GetMapping(
      path = "/testWebClient",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebClient() {
    return webClientBuilder.build().get().uri("http://order/testWebClient").retrieve()
        .bodyToMono(String.class);
  }

  @GetMapping(
      path = "/testWebClientRetry",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebClientRetry(ServerWebExchange exchange,
      @RequestParam(name = "invocationID") String invocationID) {
    return webClientBuilder.build().get()
        .uri("http://webflux/testWebClientRetry?invocationID={1}", invocationID)
        .attribute(InvocationContextHolder.ATTRIBUTE_KEY, exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY))
        .retrieve()
        .bodyToMono(String.class);
  }

  @GetMapping(
      path = "/testWebClientBulkhead",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebClientBulkhead(ServerWebExchange exchange) {
    return webClientBuilder.build().get()
        .uri("http://webflux/testWebClientBulkhead")
        .attribute(InvocationContextHolder.ATTRIBUTE_KEY, exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY))
        .retrieve()
        .bodyToMono(String.class);
  }

  @GetMapping(
      path = "/testWebClientInstanceIsolation",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebClientInstanceIsolation(ServerWebExchange exchange,
      @RequestParam(name = "invocationID") String invocationID) {
    return webClientBuilder.build().get()
        .uri("http://webflux/testWebClientInstanceIsolation?invocationID={1}", invocationID)
        .attribute(InvocationContextHolder.ATTRIBUTE_KEY, exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY))
        .retrieve()
        .bodyToMono(String.class);
  }

  @GetMapping(
      path = "/testWebClientFaultInjectionReturnNull",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebClientFaultInjectionReturnNull(ServerWebExchange exchange) {
    return webClientBuilder.build().get()
        .uri("http://webflux/testWebClientFaultInjectionReturnNull")
        .attribute(InvocationContextHolder.ATTRIBUTE_KEY, exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY))
        .retrieve()
        .bodyToMono(String.class);
  }

  @GetMapping(
      path = "/testWebClientFaultInjectionThrowException",
      produces = MediaType.APPLICATION_JSON_VALUE)
  public Mono<String> testWebClientFaultInjectionThrowException(ServerWebExchange exchange) {
    return webClientBuilder.build().get()
        .uri("http://webflux/testWebClientFaultInjectionThrowException")
        .attribute(InvocationContextHolder.ATTRIBUTE_KEY, exchange.getAttribute(InvocationContextHolder.ATTRIBUTE_KEY))
        .retrieve()
        .bodyToMono(String.class);
  }
}
