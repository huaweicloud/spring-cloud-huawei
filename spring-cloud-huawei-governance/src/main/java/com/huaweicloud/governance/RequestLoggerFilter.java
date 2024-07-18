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

package com.huaweicloud.governance;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.common.context.InvocationContext;
import com.netflix.loadbalancer.Server;

public class RequestLoggerFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestLoggerFilter.class);

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      if (!(request instanceof HttpServletRequest) || !(response instanceof HttpServletResponse)) {
        chain.doFilter(request, response);
        return;
      }
      chain.doFilter(request, response);
    } catch (Throwable e) {
      Server server = InvocationContext.getCurrentInstanse();
      if (server != null) {
        LOGGER.error("request >>>>>>>>>>>>>> service [{}:{}] failed", server.getHost(), server.getPort(), e);
      }
      throw e;
    }
  }
}
