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

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class MetricsControllerIT {
  final RestTemplate template = new RestTemplate();

  @Test
  public void testRestTemplateMetrics() {
    String result = template.getForObject(Constant.gatewayServiceUrl + "/order/order/metrics/testRestTemplate?name={1}",
        String.class, "hello");
    assertThat(result).isEqualTo("hellohello");
    try {
      result = template.getForObject(Constant.priceServiceUrl + "/price/metrics/testInvocationStage", String.class);
      assertThat(result).isEqualTo("size=2|id=GET /price/metrics/testGet|||id=POST /price/metrics/testPost||");

      result = template.getForObject(Constant.orderServiceUrl + "/order/metrics/testInvocationStage", String.class);
      assertThat(result).isEqualTo("size=1|id=GET /order/metrics/testRestTemplate|"
          + "restTemplate GET /price/metrics/testGet:restTemplate POST /price/metrics/testPost:|");
    } finally {
      result = template.getForObject(Constant.priceServiceUrl + "/price/metrics/clearInvocationStage", String.class);
      assertThat(result).isEqualTo("success");
      result = template.getForObject(Constant.orderServiceUrl + "/order/metrics/clearInvocationStage", String.class);
      assertThat(result).isEqualTo("success");
    }
  }

  @Test
  public void testFeignMetrics() {
    String result = template.postForObject(Constant.gatewayServiceUrl + "/order/order/metrics/testFeign",
        "hello", String.class);
    assertThat(result).isEqualTo("hellohello");
    try {
      result = template.getForObject(Constant.priceServiceUrl + "/price/metrics/testInvocationStage", String.class);
      assertThat(result).isEqualTo("size=2|id=GET /price/metrics/testGet|||id=POST /price/metrics/testPost||");

      result = template.getForObject(Constant.orderServiceUrl + "/order/metrics/testInvocationStage", String.class);
      assertThat(result).isEqualTo("size=1|id=POST /order/metrics/testFeign|"
          + "feign GET /price/metrics/testGet:feign POST /price/metrics/testPost:|");
    } finally {
      result = template.getForObject(Constant.priceServiceUrl + "/price/metrics/clearInvocationStage", String.class);
      assertThat(result).isEqualTo("success");
      result = template.getForObject(Constant.orderServiceUrl + "/order/metrics/clearInvocationStage", String.class);
      assertThat(result).isEqualTo("success");
    }
  }

  @Test
  public void testFeignAndRestTemplateMetrics() {
    String result = template.postForObject(Constant.gatewayServiceUrl + "/order/order/metrics/testFeignAndRestTemplate",
        "hello", String.class);
    assertThat(result).isEqualTo("hellohellohellohello");
    try {
      result = template.getForObject(Constant.priceServiceUrl + "/price/metrics/testInvocationStage", String.class);
      assertThat(result).isEqualTo("size=4|id=GET /price/metrics/testGet|||id=GET /price/metrics/testGet|||"
          + "id=POST /price/metrics/testPost|||id=POST /price/metrics/testPost||");

      result = template.getForObject(Constant.orderServiceUrl + "/order/metrics/testInvocationStage", String.class);
      assertThat(result).isEqualTo("size=1|id=POST /order/metrics/testFeignAndRestTemplate|"
          + "feign POST /price/metrics/testPost:feign POST /price/metrics/testPost@:"
          + "restTemplate GET /price/metrics/testGet:restTemplate GET /price/metrics/testGet@:|");
    } finally {
      result = template.getForObject(Constant.priceServiceUrl + "/price/metrics/clearInvocationStage", String.class);
      assertThat(result).isEqualTo("success");
      result = template.getForObject(Constant.orderServiceUrl + "/order/metrics/clearInvocationStage", String.class);
      assertThat(result).isEqualTo("success");
    }
  }
}
