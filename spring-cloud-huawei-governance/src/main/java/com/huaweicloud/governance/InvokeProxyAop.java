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

import com.huaweicloud.governance.handler.GovManager;
import com.huaweicloud.governance.marker.GovHttpRequest;
import com.huaweicloud.governance.policy.Policy;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.http.HttpHeaders;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
@Aspect
public class InvokeProxyAop {

  private static final Logger LOGGER = LoggerFactory.getLogger(InvokeProxyAop.class);

  private static final String THROWABLE_KEY = "TH";

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
    List<Policy> policies = matchersManager.match(convert(request));
    Map<String, Throwable> localContext = new HashMap<>();
    Object result = null;
    try {
      result = govManager.process(policies, () -> {
        try {
          return pjp.proceed();
        } catch (Throwable throwable) {
          localContext.put(THROWABLE_KEY, throwable);
        }
        return null;
      });
    } catch (Throwable th) {
      HttpServletResponse response = (HttpServletResponse) pjp.getArgs()[1];
      if (th instanceof RequestNotPermitted) {
        response.setStatus(502);
        response.getWriter().print("rate limit !!");
      } else {
        localContext.put(THROWABLE_KEY, th);
      }
    }
    if (result == null && localContext.containsKey(THROWABLE_KEY)) {
      throw localContext.get(THROWABLE_KEY);
    }
    return result;
  }

  private GovHttpRequest convert(HttpServletRequest request) {
    GovHttpRequest govHttpRequest = new GovHttpRequest();
    govHttpRequest.setHeaders(getHeaders(request));
    govHttpRequest.setMethod(request.getMethod());
    govHttpRequest.setUri(request.getRequestURI());
    return govHttpRequest;
  }

  private static Map<String, String> getHeaders(HttpServletRequest servletRequest) {
    Enumeration<String> headerNames = servletRequest.getHeaderNames();
    HttpHeaders httpHeaders = new HttpHeaders();
    while (headerNames.hasMoreElements()) {
      String headerName = headerNames.nextElement();
      Enumeration<String> headerValues = servletRequest.getHeaders(headerName);
      while (headerValues.hasMoreElements()) {
        httpHeaders.add(headerName, headerValues.nextElement());
      }
    }
    return httpHeaders.toSingleValueMap();
  }
}
