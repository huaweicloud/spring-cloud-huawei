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

package com.huaweicloud.common.context;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public final class InvocationContextHolder {
  public static final String SERIALIZE_KEY = "x-invocation-context";

  private static final Logger LOGGER = LoggerFactory.getLogger(InvocationContextHolder.class);

  private static final ObjectMapper MAPPER = new ObjectMapper();

  private static final ThreadLocal<InvocationContext> INVOCATION_CONTEXT = new ThreadLocal<>();

  public static InvocationContext getInvocationContext() {
    return INVOCATION_CONTEXT.get();
  }

  public static InvocationContext create(String context) {
    InvocationContext result = new InvocationContext();

    if (!StringUtils.isEmpty(context)) {
      try {
        Map<String, String> data = MAPPER.readValue(context, new TypeReference<Map<String, String>>() {
        });
        result.putContext(data);
      } catch (Exception e) {
        LOGGER.error("Create invocation context failed, build an empty one.");
      }
    }
    INVOCATION_CONTEXT.set(result);
    return result;
  }

  public static String serialize(InvocationContext context) {
    try {
      return MAPPER.writeValueAsString(context.getContext());
    } catch (Exception e) {
      LOGGER.error("Serialize invocation context failed, build an empty one.");
    }
    return "";
  }
}
