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
package com.huaweicloud.governance.adapters.webmvc;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateConsumer;
import io.vavr.CheckedConsumer;

public class BulkheadFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(BulkheadFilter.class);


  private static final Object EMPTY_HOLDER = new Object();

  private final BulkheadHandler bulkheadHandler;

  public BulkheadFilter(BulkheadHandler bulkheadHandler) {
    this.bulkheadHandler = bulkheadHandler;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (WebMvcUtils.isNotHttpServlet(request, response)) {
      chain.doFilter(request, response);
      return;
    }

    GovernanceRequestExtractor governanceRequest = WebMvcUtils.convert((HttpServletRequest) request);
    try {
      Bulkhead bulkhead = bulkheadHandler.getActuator(governanceRequest);
      if (bulkhead != null) {
        CheckedConsumer<Object> next = (v) -> chain.doFilter(request, response);
        DecorateConsumer<Object> decorateConsumer = Decorators.ofConsumer(next.unchecked());
        decorateConsumer.withBulkhead(bulkhead);
        decorateConsumer.accept(EMPTY_HOLDER);
        return;
      }
      chain.doFilter(request, response);
    } catch (Throwable e) {
      if (e instanceof BulkheadFullException) {
        ((HttpServletResponse) response).setStatus(429);
        response.getWriter().print("bulkhead is full and does not permit further calls.");
        LOGGER.warn("bulkhead is full and does not permit further calls : {}",
            e.getMessage());
      } else {
        throw e;
      }
    }
  }
}
