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
package com.huaweicloud.common.util;

import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.commons.codec.net.URLCodec;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.LinkedCaseInsensitiveMap;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class HeaderUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(HeaderUtil.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final URLCodec CODEC = new URLCodec("UTF-8");

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

  public static Map<String, String> deserialize(String content) {
    if (!StringUtils.isEmpty(content)) {
      try {
        String json = CODEC.decode(content);
        return MAPPER.readValue(json, new TypeReference<Map<String, String>>() {
        });
      } catch (Exception e) {
        LOGGER.error("Create invocation context failed, build an empty one.", e);
      }
    }
    return Collections.emptyMap();
  }

  public static String serialize(Map<String, String> content) {
    try {
      String json = MAPPER.writeValueAsString(content);
      return CODEC.encode(json);
    } catch (Exception e) {
      LOGGER.error("Serialize invocation context failed, build an empty one.", e);
    }
    return "";
  }
}
