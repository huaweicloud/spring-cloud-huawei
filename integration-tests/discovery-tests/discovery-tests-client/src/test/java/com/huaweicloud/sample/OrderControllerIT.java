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

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class OrderControllerIT {
  final String url = "http://127.0.0.1:9098";

  final int pricePort = 9090;

  final RestTemplate template = new RestTemplate();

  @Test
  public void testGetOrder() {
    String result = template.getForObject(url + "/order?id=hello", String.class);
    assertThat(result).isEqualTo("hello");
  }

  // tests can be enabled when dynamic configuration is enabled
//  @Test
//  public void testGetConfiguration() {
//    String result = template.getForObject(url + "/configuration", String.class);
//    assertThat(result).isEqualTo("[FIRST, SECOND]:name");
//  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testGetServices() {
    List result = template.getForObject(url + "/services", List.class);
    assertThat(result.size()).isGreaterThanOrEqualTo(1);
  }

  @Test
  public void testInvocationContext() {
    HttpHeaders headers = new HttpHeaders();
    headers.add("x-invocation-context", "{\"test01\":\"test01\"}");
    HttpEntity<Void> entity = new HttpEntity<>(headers);
    String result = template.exchange(url + "/invocationContext", HttpMethod.GET, entity, String.class).getBody();
    assertThat(result).isEqualTo("success");
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testGetInstances() {
    List result = template.getForObject(url + "/instances", List.class);
    assertThat(result.size()).isEqualTo(1);
    Map instance = (Map) result.get(0);
    assertThat(instance.get("port")).isEqualTo(pricePort);
  }
}
