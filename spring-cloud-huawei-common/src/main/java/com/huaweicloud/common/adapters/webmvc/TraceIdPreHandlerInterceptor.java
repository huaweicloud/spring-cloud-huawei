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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

public class TraceIdPreHandlerInterceptor implements PreHandlerInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(TraceIdPreHandlerInterceptor.class);

  private final ContextProperties contextProperties;

  public TraceIdPreHandlerInterceptor(ContextProperties contextProperties) {
    this.contextProperties = contextProperties;
  }

  @Override
  public boolean handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    if (context.getContext(InvocationContext.CONTEXT_TRACE_ID) == null) {
      context.putContext(InvocationContext.CONTEXT_TRACE_ID, InvocationContext.generateTraceId());
    }
    if (contextProperties.isEnableTraceInfo()) {
      LOGGER.info("receive request [{}] from service [{}]. trace id [{}]",
          request.getRequestURL().toString(),
          context.getContext(InvocationContext.CONTEXT_MICROSERVICE_NAME),
          context.getContext(InvocationContext.CONTEXT_TRACE_ID));
    }
    return true;
  }
}
