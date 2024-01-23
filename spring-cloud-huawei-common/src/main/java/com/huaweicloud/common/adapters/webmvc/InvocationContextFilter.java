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

package com.huaweicloud.common.adapters.webmvc;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.slf4j.MDC;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.context.InvocationStage;

public class InvocationContextFilter implements Filter {
  private final ContextProperties contextProperties;

  public InvocationContextFilter(ContextProperties contextProperties) {
    this.contextProperties = contextProperties;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    HttpServletRequest httpServletRequest = (HttpServletRequest) request;
    InvocationContext context = InvocationContextHolder.deserializeAndCreate(
        httpServletRequest.getHeader(InvocationContextHolder.SERIALIZE_KEY));

    contextProperties.getHeaderContextMapper()
        .forEach((k, v) -> {
          if (!StringUtils.isEmpty(httpServletRequest.getHeader(k))) {
            context.putContext(v, httpServletRequest.getHeader(k));
          }
        });
    contextProperties.getQueryContextMapper()
        .forEach((k, v) -> {
          if (!StringUtils.isEmpty(httpServletRequest.getParameter(k))) {
            context.putContext(v, httpServletRequest.getParameter(k));
          }
        });

    // copy or generate trace id
    if (context.getContext(InvocationContext.CONTEXT_TRACE_ID) == null) {
      context.putContext(InvocationContext.CONTEXT_TRACE_ID, InvocationContext.generateTraceId());
    }

    // Add MDC
    MDC.put(InvocationContext.CONTEXT_TRACE_ID, context.getContext(InvocationContext.CONTEXT_TRACE_ID));
    InvocationStage stage = context.getInvocationStage();
    stage.begin(buildId((HttpServletRequest) request, context));
    try {
      chain.doFilter(request, response);
    } finally {
      stage.finish(((HttpServletResponse) response).getStatus());
      MDC.remove(InvocationContext.CONTEXT_TRACE_ID);
      InvocationContextHolder.clearInvocationContext();
    }
  }

  private String buildId(HttpServletRequest request, InvocationContext context) {
    if (contextProperties.isUseContextOperationForMetrics()) {
      if (context.getContext(InvocationContext.CONTEXT_OPERATION_ID) != null) {
        return context.getContext(InvocationContext.CONTEXT_OPERATION_ID);
      }
      String id = buildOperation(request);
      context.putContext(InvocationContext.CONTEXT_OPERATION_ID, id);
      return id;
    }
    return buildOperation(request);
  }

  private String buildOperation(HttpServletRequest request) {
    return request.getMethod()
        + " "
        + request.getRequestURI();
  }
}
