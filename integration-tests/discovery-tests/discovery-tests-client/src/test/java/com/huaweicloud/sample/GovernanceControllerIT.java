/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class GovernanceControllerIT {
  String url = "http://127.0.0.1:9098";

  RestTemplate template = new RestTemplate();

  @Test
  public void testRetry() {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(url + "/govern/retry?invocationID={1}", String.class, invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }

  @Test
  public void testRetryFeign() {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(url + "/govern/retryFeign?invocationID={1}", String.class, invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }


  @Test
  public void testCircuitBreaker() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + "/govern/circuitBreaker", String.class);
              if (!"ok".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"429 : [circuitBreaker is open.]".equals(e.getMessage())
                  && !e.getMessage().contains("test error")  && !e.getMessage().startsWith("500")) {
                notExpectedFailed.set(true);
              }
              if ("429 : [circuitBreaker is open.]".equals(e.getMessage())) {
                expectedFailed.set(true);
              }
            }
            latch.countDown();
          }
        }.start();
      }
      Thread.sleep(100);
    }

    latch.await(20, TimeUnit.SECONDS);
    Assert.assertEquals(true, expectedFailed.get());
    Assert.assertEquals(false, notExpectedFailed.get());
  }

  @Test
  public void testBulkhead() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + "/govern/bulkhead", String.class);
              if (!"Hello world!".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"429 : [bulkhead is full and does not permit further calls.]".equals(e.getMessage())) {
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
    Assert.assertEquals(true, expectedFailed.get());
    Assert.assertEquals(false, notExpectedFailed.get());
  }

  @Test
  public void testRateLimiting() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(url + "/govern/hello", String.class);
              if (!"Hello world!".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"429 : [rate limited.]".equals(e.getMessage())) {
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
    Assert.assertEquals(true, expectedFailed.get());
    Assert.assertEquals(false, notExpectedFailed.get());
  }
}
