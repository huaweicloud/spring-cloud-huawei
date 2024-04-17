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

import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateConsumer;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.CheckedConsumer;

public class RateLimitingFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(IdentifierRateLimitingFilter.class);

  private static final Object EMPTY_HOLDER = new Object();

  private final RateLimitingHandler rateLimitingHandler;

  public RateLimitingFilter(RateLimitingHandler rateLimitingHandler) {
    this.rateLimitingHandler = rateLimitingHandler;
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
      RateLimiter rateLimiter = rateLimitingHandler.getActuator(governanceRequest);
      if (rateLimiter != null) {
        CheckedConsumer<Object> next = (v) -> chain.doFilter(request, response);
        DecorateConsumer<Object> decorateConsumer = Decorators.ofConsumer(next.unchecked());
        decorateConsumer.withRateLimiter(rateLimiter);
        decorateConsumer.accept(EMPTY_HOLDER);
        return;
      }
      chain.doFilter(request, response);
    } catch (Throwable e) {
      if (e instanceof RequestNotPermitted) {
        ((HttpServletResponse) response).setStatus(429);
        response.getWriter().print("rate limited.");
        LOGGER.warn("the request is rate limit by policy : {}",
            e.getMessage());
      } else {
        throw e;
      }
    }
  }
}
