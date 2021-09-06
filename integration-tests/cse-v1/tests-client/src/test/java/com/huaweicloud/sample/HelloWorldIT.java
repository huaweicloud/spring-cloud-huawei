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

import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

public class HelloWorldIT {
  RestTemplate template = new RestTemplate();

  @Test
  public void testHelloWorld() {
    for (int i = 0; i < 10; i++) {
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("canary", "old");
      HttpEntity<Object> entity = new HttpEntity<>(headers);
      String result = template
          .exchange(Config.GATEWAY_URL + "/sayHello?name=World", HttpMethod.GET, entity, String.class).getBody();
      assertThat(result).isEqualTo("Hello World");
    }
  }

  @Test
  public void testConsumerHelloWorldCanary() {
    int oldCount = 0;
    int newCount = 0;

    for (int i = 0; i < 20; i++) {
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("canary", "new");
      HttpEntity<Object> entity = new HttpEntity<>(headers);
      String result = template
          .exchange(Config.GATEWAY_URL + "/sayHelloCanary?name=World", HttpMethod.GET, entity, String.class).getBody();
      if (result.equals("Hello Canary World")) {
        oldCount++;
      } else if (result.equals("Hello Canary in canary World")) {
        newCount++;
      } else {
        Assert.fail("not expected result testHelloWorldCanary");
        return;
      }
    }

    double ratio = oldCount / (float) (oldCount + newCount);
    assertThat(ratio).isBetween(0.1, 0.3);
  }


  @Test
  public void testGatewayHelloWorldCanary() {
    int oldCount = 0;
    int newCount = 0;

    for (int i = 0; i < 20; i++) {
      MultiValueMap<String, String> headers = new HttpHeaders();
      headers.add("canary", "new");
      HttpEntity<Object> entity = new HttpEntity<>(headers);
      String result = template
          .exchange(Config.GATEWAY_URL + "/gateway/sayHelloCanary?name=World", HttpMethod.GET, entity, String.class).getBody();
      if (result.equals("Hello Gateway Canary World")) {
        oldCount++;
      } else if (result.equals("Hello Gateway Canary in canary World")) {
        newCount++;
      } else {
        Assert.fail("not expected result testHelloWorldCanary");
        return;
      }
    }

    double ratio = oldCount / (float) (oldCount + newCount);
    assertThat(ratio).isBetween(0.1, 0.3);
  }
}
