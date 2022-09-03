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

package com.huaweicloud.common.adapters.webmvc;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.huaweicloud.common.access.AccessLogLogger;
import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

public class AccessLogFilter implements Filter {
  private final ContextProperties contextProperties;

  private final AccessLogLogger accessLogLogger;

  public AccessLogFilter(ContextProperties contextProperties, AccessLogLogger accessLogLogger) {
    this.contextProperties = contextProperties;
    this.accessLogLogger = accessLogLogger;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    if (!contextProperties.isEnableTraceInfo()) {
      chain.doFilter(request, response);
      return;
    }

    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();

    String req = ((HttpServletRequest) request).getRequestURI();
    String source = context.getContext(InvocationContext.CONTEXT_MICROSERVICE_NAME) == null
        ? request.getRemoteAddr()
        : context.getContext(InvocationContext.CONTEXT_MICROSERVICE_NAME);
    accessLogLogger.log(context, "WebMVC receive request", req, source, null, 0, 0);

    long begin = System.currentTimeMillis();
    try {
      chain.doFilter(request, response);
      accessLogLogger.log(context, "WebMVC finish request",
          req, source, null, ((HttpServletResponse) response).getStatus(), System.currentTimeMillis() - begin);
    } catch (Throwable error) {
      accessLogLogger.log(context, "WebMVC finish request(" + error.getClass().getName() + ")",
          req, source, null, -1, System.currentTimeMillis() - begin);
      throw error;
    }
  }
}
