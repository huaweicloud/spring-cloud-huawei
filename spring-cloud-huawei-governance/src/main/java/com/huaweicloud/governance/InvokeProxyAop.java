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

import com.huaweicloud.common.util.HeaderUtil;
import com.huaweicloud.governance.client.track.RequestTrackContext;
import com.huaweicloud.governance.marker.GovHttpRequest;
import com.huaweicloud.governance.policy.Policy;

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
@Aspect
public class InvokeProxyAop {

  private static final Logger LOGGER = LoggerFactory.getLogger(InvokeProxyAop.class);

  private static final String RATE_LIMITING_POLICY_NAME = "RateLimitingPolicy";

  private static final String CIRCUIT_BREAKER_POLICY_NAME = "CircuitBreakerPolicy";

  private static final String BULKHEAD_POLICY_NAME = "BulkheadPolicy";

  @Autowired
  private MatchersManager matchersManager;

  @Autowired
  private GovManager govManager;

  @Pointcut("execution(* org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter.handleInternal(..))")
  public void pointCut() {
  }

  @Around("pointCut()")
  public Object aroundInvoke(ProceedingJoinPoint pjp) throws Throwable {
    HttpServletRequest request = (HttpServletRequest) pjp.getArgs()[0];
    HttpServletResponse response = (HttpServletResponse) pjp.getArgs()[1];
    Map<String, Policy> policies = matchersManager.match(convert(request));
    RequestTrackContext.setPolicies(new ArrayList(policies.values()));
    Object result = null;
    try {
      result = govManager.processServer(RequestTrackContext.getPolicies(), pjp::proceed);
    } catch (Throwable th) {
      LOGGER.debug("request error, detail info print : {}", request);
      if (th instanceof RequestNotPermitted) {
        response.setStatus(502);
        response.getWriter().print("rate limit!");
        LOGGER.warn("the request is rate limit by policy : {}",
            policies.get(RATE_LIMITING_POLICY_NAME));
      } else if (th instanceof CallNotPermittedException) {
        response.setStatus(502);
        response.getWriter().print("circuitBreaker is open!");
        LOGGER.warn("circuitBreaker is open by policy : {}",
            policies.get(CIRCUIT_BREAKER_POLICY_NAME));
      } else if (th instanceof BulkheadFullException) {
        response.setStatus(502);
        response.getWriter().print("bulkhead is full and does not permit further calls!");
        LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
            policies.get(BULKHEAD_POLICY_NAME));
      } else {
        throw th;
      }
    } finally {
      RequestTrackContext.remove();
    }
    return result;
  }

  private GovHttpRequest convert(HttpServletRequest request) {
    GovHttpRequest govHttpRequest = new GovHttpRequest();
    govHttpRequest.setHeaders(HeaderUtil.getHeaders(request));
    govHttpRequest.setMethod(request.getMethod());
    govHttpRequest.setUri(request.getRequestURI());
    return govHttpRequest;
  }
}
