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

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

public class LoadbalanceIT {
  final String url = "http://127.0.0.1:9098";

  final RestTemplate template = new RestTemplate();

  @Test
  public void testLoadbalance_template() {
    List<String> str = new ArrayList<>();
    List<String> str_beta = new ArrayList<>();
    String result = null;
    for (int i = 0; i < 10; i++) {
      result = template.getForObject(url + "/orderBal?id=hello", String.class);
      if (result.equals("hello")) {
        str.add(result);
      } else {
        str_beta.add(result);
      }
    }
    assertThat(str.size()).isEqualTo(10);
    assertThat(str_beta.size()).isEqualTo(0);
  }

  @Test
  public void testLoadbalance_feign() {
    List<String> str = new ArrayList<>();
    List<String> str_beta = new ArrayList<>();
    String result = null;
    for (int i = 0; i < 10; i++) {
      result = template.getForObject(url + "/govern/loadbalance", String.class);
      if (result.equals("I am price")) {
        str.add(result);
      } else {
        str_beta.add(result);
      }
    }
    assertThat(str.size()).isEqualTo(10);
    assertThat(str_beta.size()).isEqualTo(0);
  }
}
