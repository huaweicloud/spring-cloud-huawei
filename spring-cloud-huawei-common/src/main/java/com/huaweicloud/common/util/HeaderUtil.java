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

import org.springframework.http.HttpHeaders;

public class HeaderUtil {


  /**
   * 获取所有header的map,并将key转为小写
   *
   * 根据 RFC 7230(https://tools.ietf.org/html/rfc7230#section-3.2) 所描述
   * http header key 应该不区分大小写 ，这里统一转成小写
   *
   * @param servletRequest
   * @return
   */
  public static Map<String, String> getHeaders(HttpServletRequest servletRequest) {
    Enumeration<String> headerNames = servletRequest.getHeaderNames();
    HttpHeaders httpHeaders = new HttpHeaders();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headerValues = servletRequest.getHeaders(headerName);
      while (headerValues.hasMoreElements()) {
        httpHeaders.add(headerName.toLowerCase(), headerValues.nextElement());
      }
    }
    return httpHeaders.toSingleValueMap();
  }
}
