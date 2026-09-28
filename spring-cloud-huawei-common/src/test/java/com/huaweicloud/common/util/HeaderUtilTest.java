/*

 * Copyright (C) 2020-2026 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.common.util;

import java.lang.reflect.Proxy;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class HeaderUtilTest {

  @Test
  public void shouldSerializeAndDeserializeSpecialCharacters() {
    Map<String, String> headers = new LinkedHashMap<>();
    headers.put("x-trace-name", "订单 / order?status=已支付");
    headers.put("x-special-value", "a+b&c=d");

    String serialized = HeaderUtil.serialize(headers);

    Assertions.assertFalse(serialized.isEmpty());
    Assertions.assertEquals(headers, HeaderUtil.deserialize(serialized));
  }

  @Test
  public void shouldReturnEmptyMapForInvalidSerializedContent() {
    Assertions.assertTrue(HeaderUtil.deserialize(null).isEmpty());
    Assertions.assertTrue(HeaderUtil.deserialize("").isEmpty());
    Assertions.assertTrue(HeaderUtil.deserialize("not%2-valid-json").isEmpty());
  }

  @Test
  public void shouldReadHeadersCaseInsensitivelyAndUseFirstValue() {
    Map<String, List<String>> headers = new LinkedHashMap<>();
    headers.put("X-Request-Id", List.of("first", "second"));
    headers.put("X-Empty", Collections.emptyList());
    HttpServletRequest request = createRequest(headers);

    Map<String, String> result = HeaderUtil.getHeaders(request);

    Assertions.assertEquals(1, result.size());
    Assertions.assertEquals("first", result.get("x-request-id"));
    Assertions.assertFalse(result.containsKey("x-empty"));
  }

  private HttpServletRequest createRequest(Map<String, List<String>> headers) {
    return (HttpServletRequest) Proxy.newProxyInstance(
        HttpServletRequest.class.getClassLoader(),
        new Class<?>[] {HttpServletRequest.class},
        (proxy, method, args) -> {
          if ("getHeaderNames".equals(method.getName())) {
            return Collections.enumeration(headers.keySet());
          }
          if ("getHeaders".equals(method.getName())) {
            return Collections.enumeration(headers.getOrDefault(args[0], Collections.emptyList()));
          }
          throw new UnsupportedOperationException(method.getName());
        });
  }
}
