/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.handler.ext.ServerRecoverPolicy;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.huaweicloud.common.util.HeaderUtil;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import io.vavr.CheckedFunction0;

/**
 * Default provider governance implementation
 **/
@Aspect
public class GovernanceRequestMappingHandlerAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(GovernanceRequestMappingHandlerAdapter.class);

  @Autowired
  private RateLimitingHandler rateLimitingHandler;

  @Autowired
  private CircuitBreakerHandler circuitBreakerHandler;

  @Autowired
  private BulkheadHandler bulkheadHandler;

  @Autowired(required = false)
  private ServerRecoverPolicy<Object> serverRecoverPolicy;

  @Pointcut("execution(* org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(..))")
  public void pointCut() {
  }

  @Around("pointCut()")
  public Object aroundInvoke(ProceedingJoinPoint pjp) throws Throwable {
    HttpServletRequest request = (HttpServletRequest) pjp.getArgs()[0];
    HttpServletResponse response = (HttpServletResponse) pjp.getArgs()[1];
    GovernanceRequest governanceRequest = convert(request);

    CheckedFunction0<Object> next = pjp::proceed;
    DecorateCheckedSupplier<Object> dcs = Decorators.ofCheckedSupplier(next);

    try {
      SpringCloudInvocationContext.setInvocationContext();

      addCircuitBreaker(dcs, governanceRequest);
      addBulkhead(dcs, governanceRequest);
      addRateLimiting(dcs, governanceRequest);

      return dcs.get();
    } catch (Throwable th) {
      if (th instanceof RequestNotPermitted) {
        response.setStatus(429);
        response.getWriter().print("rate limited.");
        LOGGER.warn("the request is rate limit by policy : {}",
            th.getMessage());
      } else if (th instanceof CallNotPermittedException) {
        response.setStatus(429);
        response.getWriter().print("circuitBreaker is open.");
        LOGGER.warn("circuitBreaker is open by policy : {}",
            th.getMessage());
      } else if (th instanceof BulkheadFullException) {
        response.setStatus(429);
        response.getWriter().print("bulkhead is full and does not permit further calls.");
        LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
            th.getMessage());
      } else {
        if (serverRecoverPolicy != null) {
          return serverRecoverPolicy.apply(th);
        }
        throw th;
      }
    } finally {
      SpringCloudInvocationContext.removeInvocationContext();
    }
    return null;
  }

  private GovernanceRequest convert(HttpServletRequest request) {
    GovernanceRequest govHttpRequest = new GovernanceRequest();
    govHttpRequest.setHeaders(HeaderUtil.getHeaders(request));
    govHttpRequest.setMethod(request.getMethod());
    govHttpRequest.setUri(request.getRequestURI());
    return govHttpRequest;
  }

  private void addBulkhead(DecorateCheckedSupplier<Object> dcs, GovernanceRequest request) {
    Bulkhead bulkhead = bulkheadHandler.getActuator(request);
    if (bulkhead != null) {
      dcs.withBulkhead(bulkhead);
    }
  }

  private void addCircuitBreaker(DecorateCheckedSupplier<Object> dcs, GovernanceRequest request) {
    CircuitBreaker circuitBreaker = circuitBreakerHandler.getActuator(request);
    if (circuitBreaker != null) {
      dcs.withCircuitBreaker(circuitBreaker);
    }
  }

  private void addRateLimiting(DecorateCheckedSupplier<Object> dcs, GovernanceRequest request) {
    RateLimiter rateLimiter = rateLimitingHandler.getActuator(request);
    if (rateLimiter != null) {
      dcs.withRateLimiter(rateLimiter);
    }
  }
}
