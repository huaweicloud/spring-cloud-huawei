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
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class WebClientGovernanceIT {
  final String url = "http://127.0.0.1:10088";

  final RestTemplate template = new RestTemplate();

  @Test
  public void testWebClientRetry() {
    for (int i = 0; i < 1; i++) {
      String invocationID = UUID.randomUUID().toString();
      String result = template.getForObject(url + "/testWebClientRetry?invocationID={1}", String.class,
          invocationID);
      assertThat(result).isEqualTo("try times: 3");
    }
  }

  @Test
  public void testWebClientInstanceBulkhead() throws Exception {
    CountDownLatch latch = new CountDownLatch(9);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);

    for (int i = 0; i < 3; i++) {
      for (int j = 0; j < 3; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + "/testWebClientBulkhead", String.class);
              if (!"OK".equals(result)) {
                notExpectedFailed.set(true);
              } else {
                successCount.getAndIncrement();
              }
            } catch (Exception e) {
              if (e instanceof HttpServerErrorException && ((HttpServerErrorException) e).getStatusCode().value() == 500) {
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
  public void testWebClientInstanceIsolation() throws Exception {
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);
    AtomicLong rejectedCount = new AtomicLong(0);

    for (int i = 0; i < 100; i++) {
      try {
        String invocationID = UUID.randomUUID().toString();
        String result = template.getForObject(url + "/testWebClientInstanceIsolation?invocationID={1}", String.class,
            invocationID);
        if (!"ok".equals(result)) {
          notExpectedFailed.set(true);
        } else {
          successCount.getAndIncrement();
        }
      } catch (Exception e) {
        if (e instanceof HttpServerErrorException && ((HttpServerErrorException) e).getStatusCode().value() == 500) {
          rejectedCount.getAndIncrement();
        } else {
          notExpectedFailed.set(true);
        }
      }
    }

    Assertions.assertFalse(notExpectedFailed.get());
    Assertions.assertEquals(100, rejectedCount.get() + successCount.get());
    Assertions.assertTrue(rejectedCount.get() >= 80);
    Assertions.assertTrue(successCount.get() >= 6);
  }

  @Test
  public void testWebClientFaultInjection() {
    String result = template.getForObject(url + "/testWebClientFaultInjectionReturnNull", String.class);
    Assertions.assertEquals(null, result);
    Assertions.assertThrows(HttpServerErrorException.class,
        () -> template.getForObject(url + "/testWebClientFaultInjectionThrowException", String.class));
  }
}
