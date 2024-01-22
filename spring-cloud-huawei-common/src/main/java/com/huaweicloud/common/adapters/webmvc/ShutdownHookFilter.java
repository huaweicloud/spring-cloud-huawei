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

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.event.ClosedEventListener;
import com.huaweicloud.common.event.ClosedEventProcessor;

public class ShutdownHookFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHookFilter.class);

  private final ContextProperties contextProperties;

  private volatile boolean isShutDown = false;

  public ShutdownHookFilter(
      ContextProperties contextProperties,
      ClosedEventListener closedEventListener) {
    this.contextProperties = contextProperties;
    closedEventListener.addClosedEventProcessor(new ClosedEventProcessor() {
      @Override
      public void process() {
        close();
      }

      @Override
      public int getOrder() {
        return 200;
      }
    });
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    if (isShutDown) {
      LOGGER.warn("application is shutting down, reject request {}", ((HttpServletRequest) request).getRequestURI());
      ((HttpServletResponse) response).sendError(503, "application is shutting down, reject requests");
      return;
    }

    chain.doFilter(request, response);
  }

  private void close() {
    if (isShutDown) {
      return;
    }
    LOGGER.warn("application is shutting down, rejecting requests...");
    isShutDown = true;
    if (contextProperties.getWaitTimeForShutDownInMillis() > 0) {
      try {
        LOGGER.info("wait {}ms for requests done.", contextProperties.getWaitTimeForShutDownInMillis());
        Thread.sleep(contextProperties.getWaitTimeForShutDownInMillis());
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }
}
