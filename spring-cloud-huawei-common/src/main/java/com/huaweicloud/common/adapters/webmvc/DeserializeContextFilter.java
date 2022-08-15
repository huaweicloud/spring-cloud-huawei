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

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

public class DeserializeContextFilter implements Filter {
  private final ContextProperties contextProperties;

  public DeserializeContextFilter(ContextProperties contextProperties) {
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
        .forEach((k, v) -> context.putContext(v, httpServletRequest.getHeader(k)));
    contextProperties.getQueryContextMapper()
        .forEach((k, v) -> context.putContext(v, httpServletRequest.getParameter(k)));

    chain.doFilter(request, response);
  }
}
