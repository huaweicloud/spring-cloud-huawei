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
package com.huaweicloud.governance;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import io.github.resilience4j.reactor.retry.RetryOperator;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import reactor.core.publisher.Mono;

public class TestResilience4j {
  private int retryCounter = 0;

  @BeforeEach
  public void setUp() {
    retryCounter = 0;
  }

  @Test
  @SuppressWarnings("unchecked")
  public void testRetryWorkForNonVoidMano() {
    Mono<ResponseEntity<String>> mono = Mono.fromCallable(() -> {
      ResponseEntity<String> result;
      if (retryCounter % 2 == 0) {
        result = ResponseEntity.status(503).body("fail");
      } else {
        result = ResponseEntity.status(200).body("ok");
      }
      retryCounter++;
      return result;
    });

    Retry retry = Retry.of("es", RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.of(30, ChronoUnit.MILLIS))
        .writableStackTraceEnabled(true)
        .failAfterMaxAttempts(true)
        .retryOnException(throwable -> throwable instanceof RuntimeException)
        .retryOnResult(result -> {
          ResponseEntity<String> temp = (ResponseEntity<String>) result;
          return temp.getStatusCode().is5xxServerError();
        })
        .build());

    Assertions.assertEquals("ok", mono.transform(RetryOperator.of(retry)).block().getBody());
  }


  @Test
  public void testRetryWorkForVoidMano() {
    final Holder result = new Holder();
    Mono<Void> mono = Mono.fromRunnable(() -> {
      if (retryCounter % 2 == 0) {
        result.code = 503;
      } else {
        result.code = 200;
      }
      retryCounter++;
    });

    Retry retry = Retry.of("es", RetryConfig.custom()
        .maxAttempts(3)
        .waitDuration(Duration.of(30, ChronoUnit.MILLIS))
        .writableStackTraceEnabled(true)
        .failAfterMaxAttempts(true)
        .retryOnException(throwable -> throwable instanceof RuntimeException)
        .retryOnResult(r -> {
          Holder temp = (Holder) r;
          return temp.code == 503;
        })
        .build());

    Mono<Holder> toRun = mono.then(Mono.defer(() -> Mono.just(result)));
    mono = toRun.transform(RetryOperator.of(retry)).then();
    mono.block();
    Assertions.assertEquals(200, result.code);
  }

  public static class Holder {
    public int code;
  }
}
