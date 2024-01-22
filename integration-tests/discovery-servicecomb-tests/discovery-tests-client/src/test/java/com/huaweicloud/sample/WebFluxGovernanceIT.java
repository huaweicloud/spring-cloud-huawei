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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class WebFluxGovernanceIT {
  final String webFluxURL = "http://127.0.0.1:9099";

  final RestTemplate template = new RestTemplate();

  @Test
  public void testWebFluxServiceRateLimiting() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(webFluxURL + "/testWebFluxServiceRateLimiting", String.class);
              if (!"OK".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!e.getMessage().contains("429 Too Many Requests")) {
                notExpectedFailed.set(true);
              }
              expectedFailed.set(true);
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
  }

  @Test
  public void testWebFluxServiceIdentifierRateLimiting() throws Exception {
    for (int i = 0; i < 10; i++) {
      testWebFluxServiceIdentifierRateLimiting("user" + i);
    }
  }

  private void testWebFluxServiceIdentifierRateLimiting(String userId) throws Exception {
    CountDownLatch latch = new CountDownLatch(10);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);

    for (int i = 0; i < 1; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              HttpHeaders headers = new HttpHeaders();
              headers.add("user-id", userId);
              HttpEntity<Void> entity = new HttpEntity<>(headers);
              String result = template.exchange(webFluxURL + "/testWebFluxServiceIdentifierRateLimiting",
                  HttpMethod.GET, entity,
                  String.class).getBody();
              if (!"OK".equals(result)) {
                notExpectedFailed.set(true);
              } else {
                successCount.getAndIncrement();
              }
            } catch (Exception e) {
              if (e instanceof HttpClientErrorException && ((HttpClientErrorException) e).getStatusCode().value() == 429) {
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
    Assertions.assertTrue(successCount.get() >= 2);
  }

  @Test
  public void testWebFluxServiceCircuitBreaker() throws Exception {
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);
    AtomicLong failCount = new AtomicLong(0);
    AtomicLong rejectedCount = new AtomicLong(0);

    for (int i = 0; i < 100; i++) {
      try {
        HttpHeaders headers = new HttpHeaders();
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        String result = template.exchange(webFluxURL + "/testWebFluxServiceCircuitBreaker", HttpMethod.GET, entity,
            String.class).getBody();
        if (!"OK".equals(result)) {
          notExpectedFailed.set(true);
        } else {
          successCount.getAndIncrement();
        }
      } catch (Exception e) {
        if (e instanceof HttpServerErrorException && ((HttpServerErrorException) e).getStatusCode().value() == 503) {
          rejectedCount.getAndIncrement();
        } else if (e instanceof HttpServerErrorException && ((HttpServerErrorException) e).getStatusCode().value() == 500) {
          failCount.getAndIncrement();
        } else {
          notExpectedFailed.set(true);
        }
      }
    }

    Assertions.assertFalse(notExpectedFailed.get());
    Assertions.assertEquals(100, rejectedCount.get() + successCount.get() + failCount.get());
    Assertions.assertTrue(rejectedCount.get() >= 90);
    Assertions.assertTrue(successCount.get() >= 6);
    Assertions.assertTrue(failCount.get() >= 3);
  }

  @Test
  public void testWebFluxServiceBulkhead() throws Exception {
    CountDownLatch latch = new CountDownLatch(10);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);
    AtomicLong rejectedCount = new AtomicLong(0);

    for (int j = 0; j < 10; j++) {
      String name = "t-" + j;
      new Thread(name) {
        public void run() {
          try {
            HttpHeaders headers = new HttpHeaders();
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            String result = template.exchange(webFluxURL + "/testWebFluxServiceBulkhead", HttpMethod.GET, entity,
                String.class).getBody();
            if (!"OK".equals(result)) {
              notExpectedFailed.set(true);
            } else {
              successCount.getAndIncrement();
            }
          } catch (Exception e) {
            if (e instanceof HttpClientErrorException && ((HttpClientErrorException) e).getStatusCode().value() == 429) {
              rejectedCount.getAndIncrement();
            } else {
              notExpectedFailed.set(true);
            }
          }
          latch.countDown();
        }
      }.start();
    }

    latch.await(20, TimeUnit.SECONDS);
    Assertions.assertTrue(rejectedCount.get() >= 2);
    Assertions.assertFalse(notExpectedFailed.get());
    Assertions.assertTrue(successCount.get() >= 2);
    Assertions.assertTrue(successCount.get() + rejectedCount.get() == 10);
  }
}
