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
package com.huaweicloud.router.client.feign;

import com.huaweicloud.router.client.track.RouterTrackContext;
import feign.Request;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;

@Aspect
public class RouterFeignClientFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(RouterFeignClientFilter.class);

  @Pointcut("execution(* org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient.execute(..))")
  public void pointFeignClient() {
  }

  @Before("pointFeignClient()")
  private void invokeServiceName(JoinPoint joinPoint) {
    Request request = (Request) joinPoint.getArgs()[0];
    URI uri = URI.create(request.url());

    try {
      RouterTrackContext.setServiceName(uri.getHost());
    } catch (Throwable throwable) {
      LOGGER.error("fail to add service name into RouterTrackContext.");
    }
  }
}
