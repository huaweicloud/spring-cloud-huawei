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

package com.huaweicloud.gateway.governance;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.router.client.track.RouterTrackContext;

/**
 * intercept LoadBalanceClientFilter and add request information to router client
 * for spring cloud gateway.
 */
@Aspect
public class LoadBalanceClientFilterAspect {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalanceClientFilterAspect.class);

  @Pointcut("execution(* org.springframework.cloud.gateway.filter.LoadBalancerClientFilter.filter(..))")
  public void pointLoadBalanceClientFilter() {
  }

  @Before("pointLoadBalanceClientFilter()")
  private void setRequestContext(JoinPoint joinPoint) {
    ServerWebExchange exchange = (ServerWebExchange) joinPoint.getArgs()[0];

    try {
      RouterTrackContext.setRequestHeader(exchange.getRequest().getHeaders().toSingleValueMap());
    } catch (Throwable throwable) {
      LOGGER.error("fail to add request headers into RouterTrackContext.");
    }
  }
}
