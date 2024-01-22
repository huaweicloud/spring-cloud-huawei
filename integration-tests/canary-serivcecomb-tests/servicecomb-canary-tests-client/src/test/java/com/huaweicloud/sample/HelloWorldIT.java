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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HelloWorldIT {
  final RestTemplate template = new RestTemplate();

  @Test
  public void testConsumerHelloWorldCanary() {
    for (int i = 0; i < 10; i++) {
      String result = template.getForObject(Config.GATEWAY_URL + "/canary-consumer/sayHelloCanary?name={1}", String.class, "World");
      assertThat(result).startsWith("beta");
    }
  }

  @Test
  public void testGatewayHelloWorldCanary() {
    for (int i = 0; i < 10; i++) {
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("canary", "old");
      HttpEntity<Object> entity = new HttpEntity<>(headers);
      String result = template
          .exchange(Config.GATEWAY_URL + "/canary-provider/sayHelloCanary?name=World", HttpMethod.GET, entity, String.class).getBody();
      assertThat(result).startsWith("hello");
    }
  }


  @Test
  public void testHeaderHelloWorldCanary() {
    int oldCount = 0;
    int newCount = 0;

    for (int i = 0; i < 20; i++) {
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("canary", "new");
      HttpEntity<Object> entity = new HttpEntity<>(headers);
      String result = template
          .exchange(Config.GATEWAY_URL + "/canary-provider/sayHelloCanary?name=World", HttpMethod.GET, entity, String.class).getBody();
      if (result.startsWith("hello")) {
        oldCount++;
      } else if (result.startsWith("beta")) {
        newCount++;
      } else {
        Assertions.fail("not expected result testHelloWorldCanary");
        return;
      }
    }

    double ratio = oldCount / (float) (oldCount + newCount);
    assertThat(ratio).isBetween(0.1, 0.3);
  }

  @Test
  public void testContextSayHelloCanary() {
    for (int i = 0; i < 10; i++) {
      String result = template
          .getForObject(Config.GATEWAY_URL + "/canary-provider/contextSayHelloCanary?canary=old", String.class);
      assertThat(result).startsWith("hello");
    }
  }

  @Test
  public void testHeaderContextSayHelloCanary() {
    int oldCount = 0;
    int newCount = 0;

    for (int i = 0; i < 20; i++) {
      String result = template
          .getForObject(Config.GATEWAY_URL + "/canary-provider/contextSayHelloCanary?canary=new", String.class);
      if (result.startsWith("hello")) {
        oldCount++;
      } else if (result.startsWith("beta")) {
        newCount++;
      } else {
        Assertions.fail("not expected result testHelloWorldCanary");
        return;
      }
    }

    double ratio = oldCount / (float) (oldCount + newCount);
    assertThat(ratio).isBetween(0.1, 0.3);
  }

  @Test
  public void testRetryOnSameZeroCanary() {
    int failedCount = 0;
    int successCount = 0;
    for (int i = 0; i < 10; i++) {
      try {
        String result = template
            .getForObject(Config.GATEWAY_URL + "/canary-provider/retryOnSameZeroCanary", String.class);
        if ("ok".equals(result)) {
          successCount++;
        }
      } catch (Exception e) {
        failedCount++;
      }
    }
    Assertions.assertTrue(failedCount == 0 && successCount == 10);
  }

  @Test
  public void testRetryOnSameOneCanary() {
    int providerCount = 0;
    int betaCount = 0;
    for (int i = 0; i < 10; i++) {
      String result = template
          .getForObject(Config.GATEWAY_URL + "/canary-provider/retryOnSameOneCanary", String.class);
      if ("ok".equals(result)) {
        providerCount++;
      } else if (result.startsWith("beta")) {
        betaCount++;
      }
    }
    Assertions.assertTrue(providerCount == betaCount);
  }

  @Test
  public void testRetryOnSameAllCanary() {
    int failedCount = 0;
    int successCount = 0;
    for (int i = 0; i < 10; i++) {
      try {
        String result = template
            .getForObject(Config.GATEWAY_URL + "/canary-provider/testRetryOnSameAllCanary", String.class);
        if ("ok".equals(result)) {
          successCount++;
        }
      } catch (Exception e) {
        failedCount++;
      }
    }
    Assertions.assertTrue(failedCount == successCount);
  }
}
