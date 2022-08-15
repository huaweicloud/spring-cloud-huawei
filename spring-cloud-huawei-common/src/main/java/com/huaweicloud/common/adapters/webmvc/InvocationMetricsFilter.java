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
import java.util.concurrent.TimeUnit;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;

import com.huaweicloud.common.metrics.InvocationMetrics;

public class InvocationMetricsFilter implements Filter {
  private final InvocationMetrics invocationMetrics;

  public InvocationMetricsFilter(InvocationMetrics invocationMetrics) {
    this.invocationMetrics = invocationMetrics;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    long start = System.currentTimeMillis();
    String operation = buildOperation((HttpServletRequest) request);

    try {
      chain.doFilter(request, response);
      if (HttpStatus.valueOf(((HttpServletResponse) response).getStatus()).is5xxServerError()) {
        this.invocationMetrics.recordFailedCall(operation,
            System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
        return;
      }
      this.invocationMetrics.recordSuccessfulCall(operation,
          System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
    } catch (Exception e) {
      this.invocationMetrics.recordFailedCall(operation,
          System.currentTimeMillis() - start, TimeUnit.MILLISECONDS);
      throw e;
    }
  }

  private String buildOperation(HttpServletRequest request) {
    StringBuilder sb = new StringBuilder();
    sb.append(request.getMethod());
    sb.append(" ");
    sb.append(request.getRequestURI());
    return sb.toString();
  }
}
