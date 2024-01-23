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

package com.huaweicloud.common.access;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;

public class AccessLogLogger {
  private static final Logger LOGGER = LoggerFactory.getLogger("access_logger");

  private final ContextProperties contextProperties;

  public AccessLogLogger(ContextProperties contextProperties) {
    this.contextProperties = contextProperties;
  }

  public void log(InvocationContext context,
      String request, int status, long time) {
    if (!this.contextProperties.isEnableTraceInfo()) {
      return;
    }

    if (this.contextProperties.getTraceLevel() == null) {
      LOGGER.info(buildFormat(),
          buildArguments(context, request, status, time));
      return;
    }

    if ("INFO".equals(this.contextProperties.getTraceLevel())) {
      LOGGER.warn(buildFormat(),
          buildArguments(context, request, status, time));
      return;
    }

    if ("WARN".equals(this.contextProperties.getTraceLevel())) {
      LOGGER.warn(buildFormat(),
          buildArguments(context, request, status, time));
      return;
    }

    if ("ERROR".equals(this.contextProperties.getTraceLevel())) {
      LOGGER.error(buildFormat(),
          buildArguments(context, request, status, time));
      return;
    }

    if ("DEBUG".equals(this.contextProperties.getTraceLevel())) {
      LOGGER.debug(buildFormat(),
          buildArguments(context, request, status, time));
      return;
    }

    LOGGER.info(buildFormat(),
        buildArguments(context, request, status, time));
  }

  private String buildFormat() {
    final String format;
    StringBuilder result = new StringBuilder();
    result.append("|");

    if (contextProperties.getTraceContexts() != null) {
      for (int i = 0; i < contextProperties.getTraceContexts().size(); i++) {
        result.append("{}|");
      }
    }
    result.append("{}|{}|{}|{}|");

    format = result.toString();
    return format;
  }

  private Object[] buildArguments(InvocationContext context,
      String request, int status, long time) {
    List<Object> result = new ArrayList<>(10);

    if (contextProperties.getTraceContexts() != null) {
      for (String item : contextProperties.getTraceContexts()) {
        result.add(context.getContext(item) == null ? "" : context.getContext(item));
      }
    }
    result.add(context.getContext(InvocationContext.CONTEXT_TRACE_ID));
    result.add(status);
    result.add(time);
    result.add(request);

    return result.toArray(new Object[0]);
  }
}
