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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;
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

public class GovernanceControllerIT {
  final String orderServiceUrl = "http://127.0.0.1:9098";

  final String priceServiceUrl = "http://127.0.0.1:9090";

  final RestTemplate template = new RestTemplate();

  @Test
  public void testRetry() {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(orderServiceUrl + "/govern/retry?invocationID={1}", String.class,
        invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }

  @Test
  public void testRetryMore() {
    for (int i = 0; i < 5; i++) {
      String invocationID = UUID.randomUUID().toString();
      String result = template.getForObject(orderServiceUrl + "/govern/retryMore?invocationID={1}", String.class,
          invocationID);
      assertThat(result).isEqualTo("try times: 6");
    }
  }

  @Test
  public void testServiceNameRetry() {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(orderServiceUrl + "/govern/serviceNameRetry?invocationID={1}", String.class,
            invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }

  @Test
  public void testRetryFeign() {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(orderServiceUrl + "/govern/retryFeign?invocationID={1}", String.class,
        invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }

  @Test
  public void testRetryFeignMore() {
    for (int i = 0; i < 5; i++) {
      String invocationID = UUID.randomUUID().toString();
      String result = template.getForObject(orderServiceUrl + "/govern/retryFeignMore?invocationID={1}", String.class,
          invocationID);
      assertThat(result).isEqualTo("try times: 6");
    }
  }

  @Test
  public void testServiceNameRetryFeign() {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(orderServiceUrl + "/govern/serviceNameRetryFeign?invocationID={1}", String.class,
            invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }

  @Test
  public void testIsolationForceOpen() {
    Assertions.assertThrows(HttpServerErrorException.class,
        () -> template.getForObject(orderServiceUrl + "/govern/isolationForceOpen", String.class));
  }

  @Test
  public void testIsolationResponseHeader() {
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);
    AtomicLong rejectedCount = new AtomicLong(0);

    for (int i = 0; i < 100; i++) {
      try {
        String result = template.getForObject(orderServiceUrl + "/govern/testIsolationResponseHeader", String.class);
        if (!"success".equals(result)) {
          notExpectedFailed.set(true);
        } else {
          successCount.getAndIncrement();
        }
      } catch (Exception e) {
        if (e instanceof HttpServerErrorException && ((HttpServerErrorException) e).getStatusCode().value() == 503) {
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
  public void testIsolationForceOpenFeign() {
    // exceptions thrown by feign is catch by spring mvc and will throw of error 500
    Assertions.assertThrows(HttpServerErrorException.class,
        () -> template.getForObject(orderServiceUrl + "/govern/isolationForceOpenFeign", String.class));
  }

  @Test
  public void testIsolationResponseHeaderFeign() {
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);
    AtomicLong rejectedCount = new AtomicLong(0);

    for (int i = 0; i < 100; i++) {
      try {
        String result = template.getForObject(orderServiceUrl + "/govern/testIsolationResponseHeaderFeign", String.class);
        if (!"success".equals(result)) {
          notExpectedFailed.set(true);
        } else {
          successCount.getAndIncrement();
        }
      } catch (Exception e) {
        if (e instanceof HttpServerErrorException && ((HttpServerErrorException) e).getStatusCode().value() == 503) {
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
              String result = template.getForObject(orderServiceUrl + "/govern/circuitBreaker", String.class);
              if (!"ok".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"429 : \"circuitBreaker is open.\"".equals(e.getMessage())
                  && !e.getMessage().contains("test error") && !e.getMessage().startsWith("500")) {
                notExpectedFailed.set(true);
              }
              if ("429 : \"circuitBreaker is open.\"".equals(e.getMessage())) {
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
    Assertions.assertTrue(expectedFailed.get());
    Assertions.assertFalse(notExpectedFailed.get());
  }

  @Test
  public void testCircuitBreakerHeader() {
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);
    AtomicLong successCount = new AtomicLong(0);
    AtomicLong rejectedCount = new AtomicLong(0);

    for (int i = 0; i < 100; i++) {
      try {
        String result = template.getForObject(orderServiceUrl + "/govern/circuitBreakerHeader", String.class);
        if (!"success".equals(result)) {
          notExpectedFailed.set(true);
        } else {
          successCount.getAndIncrement();
        }
      } catch (Exception e) {
        if ("429 : \"circuitBreaker is open.\"".equals(e.getMessage())) {
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
              String result = template.getForObject(orderServiceUrl + "/govern/bulkhead", String.class);
              if (!"Hello world!".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"429 : \"bulkhead is full and does not permit further calls.\"".equals(e.getMessage())) {
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
              String result = template.getForObject(orderServiceUrl + "/govern/hello", String.class);
              if (!"Hello world!".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"429 : \"rate limited.\"".equals(e.getMessage())) {
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
  public void testRateLimitingForServiceOther() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(priceServiceUrl + "/rate/testRateLimitForService", String.class);
              if (!"success".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              if (!"429 : \"rate limited.\"".equals(e.getMessage())) {
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
    Assertions.assertFalse(expectedFailed.get());
    Assertions.assertFalse(notExpectedFailed.get());
  }

  @Test
  public void testRateLimitingForServiceOrder() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(orderServiceUrl + "/govern/rate/testRateLimitForService",
                  String.class);
              if (!"success".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              // client 429 error will report 500 error by provider
              if (!(e.getMessage().contains("500") && e.getMessage().contains("testRateLimitForService"))) {
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
  public void testIdentifierRateLimiting() throws Exception {
    for (int i = 0; i < 10; i++) {
      testIdentifierRateLimiting("user" + i);
    }
  }

  private void testIdentifierRateLimiting(String userId) throws Exception {
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
              headers.add("user-name", userId);
              HttpEntity<Void> entity = new HttpEntity<>(headers);
              String result = template.exchange(orderServiceUrl + "/govern/identifierRateLimiting", HttpMethod.GET,
                  entity,
                  String.class).getBody();
              if (!"identifierRateLimiting".equals(result)) {
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
    Assertions.assertTrue(successCount.get() >= 2);
  }

  @Test
  public void testIdentifierRateLimitingService() throws Exception {
    CountDownLatch latch = new CountDownLatch(100);
    AtomicBoolean expectedFailed = new AtomicBoolean(false);
    AtomicBoolean notExpectedFailed = new AtomicBoolean(false);

    for (int i = 0; i < 10; i++) {
      for (int j = 0; j < 10; j++) {
        String name = "t-" + i + "-" + j;
        new Thread(name) {
          public void run() {
            try {
              String result = template.getForObject(orderServiceUrl + "/govern/identifierRateLimitingService",
                  String.class);
              if (!"success".equals(result)) {
                notExpectedFailed.set(true);
              }
            } catch (Exception e) {
              // client 429 error will report 500 error by provider
              if (!(e.getMessage().contains("500") && e.getMessage().contains("identifierRateLimitingService"))) {
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
  public void testServiceFaultInjectionConsumerRestTemplate() {
    // spring decoder not properly decode json null and here will get string `null`
    Assertions.assertEquals(null,
            template.getForObject(orderServiceUrl + "/govern/serviceFaultInjectionRestTemplate", String.class));
  }


  @Test
  public void testServiceNameFaultInjectionConsumerFeign() {
    // spring decoder not properly decode json null and here will get string `null`
    Assertions.assertEquals(null,
            template.getForObject(orderServiceUrl + "/govern/serviceNameFaultInjectionFeign", String.class));
  }

  @Test
  public void testFeignFaultInjection() {
    Assertions.assertEquals(null,
        template.getForObject(orderServiceUrl + "/govern/testFeignFaultInjection?name=tom", String.class));
  }

  @Test
  public void testTemplateFaultInjection() {
    Assertions.assertEquals(null,
        template.getForObject(orderServiceUrl + "/govern/testTemplateFaultInjection?name=tom", String.class));
  }
}
