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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class GatewayGovernanceIT {
  final RestTemplate template = new RestTemplate();

  @Test
  public void testRateLimiting() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(Config.GATEWAY_URL + "/govern/rateLimiting", String.class);
              if (!"rateLimiting".equals(result)) {
                notExpectedFailed.set(true);
              } else {
                successCount.getAndIncrement();
              }
            } catch (Exception e) {
              if (e instanceof HttpClientErrorException && ((HttpClientErrorException) e).getRawStatusCode() == 429) {
                expectedFailed.set(true);
              } else {
                notExpectedFailed.set(true);
              }
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }

    latch.await(20, TimeUnit.SECONDS);
    Assertions.assertTrue(expectedFailed.get());
    Assertions.assertFalse(notExpectedFailed.get());
    Assertions.assertTrue(successCount.get() >= 10);
  }

  @Test
  public void testCircuitBreaker() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed502 = new AtomicBoolean(false);
    AtomicBoolean expectedFailed503 = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong failedCount = new AtomicLong(0);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(Config.GATEWAY_URL + "/govern/circuitBreaker", String.class);
              if (!"ok".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (e instanceof HttpServerErrorException && ((HttpServerErrorException) e).getRawStatusCode() == 502) {
                expectedFailed502.set(true);
              } else if (e instanceof HttpServerErrorException
                  && ((HttpServerErrorException) e).getRawStatusCode() == 503) {
                expectedFailed503.set(true);
                failedCount.getAndIncrement();
              } else {
                notExpectedFailed.set(true);
              }
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(300);
    }

    latch.await(20, TimeUnit.SECONDS);
    Assertions.assertTrue(expectedFailed502.get());
    Assertions.assertTrue(expectedFailed503.get());
    Assertions.assertFalse(notExpectedFailed.get());
    Assertions.assertTrue(failedCount.get() >= 90);
  }

  @Test
  public void testBulkhead() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(Config.GATEWAY_URL + "/govern/bulkhead", String.class);
              if (!"bulkhead".equals(result)) {
                notExpectedFailed.set(true);
              } else {
                successCount.getAndIncrement();
              }
            } catch (Exception e) {
              if (e instanceof HttpClientErrorException && ((HttpClientErrorException) e).getRawStatusCode() == 429) {
                expectedFailed.set(true);
              } else {
                notExpectedFailed.set(true);
              }
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }

    latch.await(20, TimeUnit.SECONDS);
    Assertions.assertTrue(expectedFailed.get());
    Assertions.assertFalse(notExpectedFailed.get());
    Assertions.assertTrue(successCount.get() >= 50);
  }

  @Test
  public void testRetry() {
    String invocationID = UUID.randomUUID().toString();
    String result = template
        .getForObject(Config.GATEWAY_URL + "/govern/retry?invocationID={1}", String.class, invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }
}
