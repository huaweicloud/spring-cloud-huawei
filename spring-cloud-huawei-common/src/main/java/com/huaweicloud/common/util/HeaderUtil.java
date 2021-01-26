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
package com.huaweicloud.common.util;

import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.LinkedCaseInsensitiveMap;

public class HeaderUtil {
  public static Map<String, String> getHeaders(HttpServletRequest servletRequest) {
    Enumeration<String> headerNames = servletRequest.getHeaderNames();
    Map<String, String> result = new LinkedCaseInsensitiveMap<>();

    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headerValues = servletRequest.getHeaders(headerName);
      // 多个 value 的情况下， 只取其中一个
      if (headerValues.hasMoreElements()) {
        result.put(headerName, headerValues.nextElement());
      }
    }

    return result;
  }
}
