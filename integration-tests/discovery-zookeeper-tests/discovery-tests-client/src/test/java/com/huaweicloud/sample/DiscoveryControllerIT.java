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

import java.util.List;
import java.util.Map;

import org.apache.commons.codec.net.URLCodec;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

public class DiscoveryControllerIT {
  final String pricePort = "9090";

  final RestTemplate template = new RestTemplate();

  @Test
  public void testGatewayGetOrder() {
    String result = template.getForObject(Constant.gatewayServiceUrl + "/order/order?id=hello", String.class);
    assertThat(result).isEqualTo("hello");
  }

  @Test
  public void testGetOrder() {
    String result = template.getForObject(Constant.orderServiceUrl + "/order?id=hello", String.class);
    assertThat(result).isEqualTo("hello");
  }

  @Test
  public void testGetOrderFeign() {
    String result = template.getForObject(Constant.orderServiceUrl + "/orderFeign?id=hello", String.class);
    assertThat(result).isEqualTo("hello");
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testGetInstances() {
    List result = template.getForObject(Constant.orderServiceUrl + "/instances", List.class);
    assertThat(result.size()).isEqualTo(1);
    Map instance = (Map) result.get(0);
    assertThat(instance.get("port").toString()).isEqualTo(pricePort);
  }
}
