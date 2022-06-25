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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

public class GatewayIT {
  final String url = "http://127.0.0.1:10088";

  final RestTemplate template = new RestTemplate();

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
}
