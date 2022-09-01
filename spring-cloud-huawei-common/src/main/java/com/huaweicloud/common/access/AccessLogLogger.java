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

package com.huaweicloud.common.access;

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

  public void log(InvocationContext context, String event,
      String request, String source, String target, int status, long time) {
    log(String.format("%1$s|%2$s|%3$s|%4$s|%5$d|%6$1d|%7$s",
        context.getContext(InvocationContext.CONTEXT_TRACE_ID),
        event,
        source == null ? "" : source,
        target == null ? "" : target,
        status,
        time,
        request));
  }

  private void log(String format, Object... arguments) {
    if (this.contextProperties.getTraceLevel() == null) {
      LOGGER.info(format, arguments);
      return;
    }

    if ("WARN".equals(this.contextProperties.getTraceLevel())) {
      LOGGER.warn(format, arguments);
      return;
    }

    if ("ERROR".equals(this.contextProperties.getTraceLevel())) {
      LOGGER.error(format, arguments);
      return;
    }

    if ("DEBUG".equals(this.contextProperties.getTraceLevel())) {
      LOGGER.debug(format, arguments);
      return;
    }

    LOGGER.info(format, arguments);
  }
}
