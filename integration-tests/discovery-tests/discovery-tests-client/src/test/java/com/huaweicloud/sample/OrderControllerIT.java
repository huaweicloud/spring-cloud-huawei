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

import java.util.List;
import java.util.Map;

import org.junit.Test;
import org.springframework.web.client.RestTemplate;

public class OrderControllerIT {
  String url = "http://127.0.0.1:8088";

  int pricePort = 8080;

  RestTemplate template = new RestTemplate();

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
  @SuppressWarnings({"rawTypes", "unckecked"})
  public void testGetServices() {
    List result = template.getForObject(url + "/services", List.class);
    assertThat(result.size()).isGreaterThan(1);
  }

  @Test
  @SuppressWarnings({"rawTypes", "unckecked"})
  public void testGetInstances() {
    List result = template.getForObject(url + "/instances", List.class);
    assertThat(result.size()).isEqualTo(1);
    Map instance = (Map) result.get(0);
    assertThat(instance.get("port")).isEqualTo(pricePort);
  }
}
