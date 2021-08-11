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

public class ConsumerConfigIT {
  RestTemplate template = new RestTemplate();

  @Test
  public void testConfig() {
    String result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v1.test.foo", String.class);
    assertThat(result).isEqualTo("foo");
    result = template.getForObject(Config.GATEWAY_URL + "/config?key=cse.v1.test.bar", String.class);
    assertThat(result).isEqualTo("bar");
  }

  @Test
  public void testFooBar() {
    String result = template.getForObject(Config.GATEWAY_URL + "/foo", String.class);
    assertThat(result).isEqualTo("foo");
    result = template.getForObject(Config.GATEWAY_URL + "/bar", String.class);
    assertThat(result).isEqualTo("bar");
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testSequences() {
    List<String> result = template.getForObject(Config.GATEWAY_URL + "/sequences", List.class);
    assertThat(result.toString()).isEqualTo("[s0, s1]");
  }

  @Test
  @SuppressWarnings({"rawtypes", "unchecked"})
  public void testModels() {
    List<Map<?, ?>> result = template.getForObject(Config.GATEWAY_URL + "/models", List.class);
    assertThat(result.toString()).isEqualTo("[{name=s1, index=2}, {name=s2, index=3}]");
  }
}
