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

public class GatewayIT {
  final String url = "http://127.0.0.1:10088";

  final RestTemplate template = new RestTemplate();

  @Test
  public void testContextMapper() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("header-context", "h1");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template.exchange(url + "/order/testContextMapper/?query-context=q1", HttpMethod.GET, entity,
        String.class).getBody();
    assertThat(result).isEqualTo("q1h1q1");
  }

  @Test
  public void testGetOrder() {
    String result = template.getForObject(url + "/order/order?id=hello", String.class);
    assertThat(result).isEqualTo("hello");
  }

  @Test
  public void testInvocationContext() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("x-invocation-context", "{\"test01\":\"test01\"}");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template.exchange(url + "/order/invocationContext", HttpMethod.GET, entity, String.class).getBody();
    assertThat(result).isEqualTo("success");
  }

  @Test
  public void testinvocationContextGateway() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("x-invocation-context", "{\"test01\":\"test01\"}");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template.exchange(url + "/order/invocationContextGateway", HttpMethod.GET, entity, String.class)
        .getBody();
    assertThat(result).isEqualTo("success");
  }

  @Test
  public void testRetry() {
    String invocationID = UUID.randomUUID().toString();
    String result = template.getForObject(url + "/gateway/retry?invocationID={1}", String.class,
        invocationID);
    assertThat(result).isEqualTo("try times: 3");
  }

  @Test
  public void gatewayIsolationForceOpenFeign() {
    Assertions.assertThrows(HttpServerErrorException.class,
        () -> template.getForObject(url + "/order/govern/gatewayIsolationForceOpenFeign", String.class));
  }

  @Test
  public void testFaultInjectionGateway() {
    long begin = System.currentTimeMillis();
    String result = template.getForObject(url + "/price/faultInjection", String.class);
    Assertions.assertEquals("success", result);
    Assertions.assertTrue((System.currentTimeMillis() - begin) >= 1000);
  }

  @Test
  public void testFaultInjectionConsumerRestTemplate() {
    // spring decoder not properly decode json null and here will get string `null`
    Assertions.assertEquals("null",
        template.getForObject(url + "/order/govern/faultInjectionRestTemplate", String.class));
  }

  @Test
  public void testFaultInjectionConsumerFeign() {
    // spring decoder not properly decode json null and here will get string `null`
    Assertions.assertEquals("null",
        template.getForObject(url + "/order/govern/faultInjectionFeign", String.class));
  }

  @Test
  public void testFaultInjectionConsumerRestTemplateModel() {
    // spring decoder not properly decode json null and here will get string `null`
    Assertions.assertNull(
        template.getForObject(url + "/order/govern/faultInjectionRestTemplateModel", PojoModel.class));
  }

  @Test
  public void testFaultInjectionConsumerFeignModel() {
    // spring decoder not properly decode json null and here will get string `null`
    Assertions.assertNull(template.getForObject(url + "/order/govern/faultInjectionFeignModel", PojoModel.class));
  }

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
              String result = template.getForObject(url + "/order/govern/rateLimiting", String.class);
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
              headers.add("user-id", userId);
              HttpEntity<Void> entity = new HttpEntity<>(headers);
              String result = template.exchange(url + "/identifierRateLimiting", HttpMethod.GET, entity,
                  String.class).getBody();
              if (!"OK".equals(result)) {
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
}
