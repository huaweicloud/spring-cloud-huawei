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

import java.util.ArrayList;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.governance.GovManager;
import org.apache.servicecomb.governance.MatchersManager;
import org.apache.servicecomb.governance.marker.GovHttpRequest;
import org.apache.servicecomb.governance.policy.Policy;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.common.util.HeaderUtil;
import com.huaweicloud.governance.client.track.RequestTrackContext;

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
    Map<String, Policy> policies = matchersManager.matchByPolicy(convert(request));
    if (CollectionUtils.isEmpty(policies)) {
      return pjp.proceed();
    }
    RequestTrackContext.setPolicies(new ArrayList(policies.values()));
    Object result = null;
    try {
      result = govManager.processServer(RequestTrackContext.getPolicies(), pjp::proceed);
    } catch (Throwable th) {
      LOGGER.debug("request error, detail info print : {}", request);
      if (th instanceof RequestNotPermitted) {
        response.setStatus(429);
        response.getWriter().print("rate limited.");
        LOGGER.warn("the request is rate limit by policy : {}",
            policies.get(RateLimitProperties.class.getName()));
      } else if (th instanceof CallNotPermittedException) {
        response.setStatus(429);
        response.getWriter().print("circuitBreaker is open.");
        LOGGER.warn("circuitBreaker is open by policy : {}",
            policies.get(CircuitBreakerProperties.class.getName()));
      } else if (th instanceof BulkheadFullException) {
        response.setStatus(429);
        response.getWriter().print("bulkhead is full and does not permit further calls.");
        LOGGER.warn("bulkhead is full and does not permit further calls by policy : {}",
            policies.get(BulkheadProperties.class.getName()));
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
