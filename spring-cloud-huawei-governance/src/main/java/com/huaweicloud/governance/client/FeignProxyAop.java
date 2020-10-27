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
package com.huaweicloud.governance.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

import com.huaweicloud.governance.MatchersManager;
import com.huaweicloud.governance.client.track.RequestTrackContext;
import com.huaweicloud.governance.handler.GovManager;
import com.huaweicloud.governance.policy.Policy;

@Aspect
public class FeignProxyAop {

  private static final String THROWABLE_KEY = "TH";

  @Autowired
  private MatchersManager matchersManager;

  @Autowired
  private GovManager govManager;

  @Pointcut("execution(* org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient.execute(..))")
  public void pointCut() {
  }

  @Around("pointCut()")
  public Object aroundInvoke(ProceedingJoinPoint pjp) throws Throwable {
    List<Policy> policies = RequestTrackContext.getPolicies();
    Map<String, Throwable> localContext = new HashMap<>();
    Object result = govManager.processClient(policies, () -> {
      try {
        return pjp.proceed();
      } catch (Throwable throwable) {
        localContext.put(THROWABLE_KEY, throwable);
      }
      return null;
    });
    if (result == null && localContext.containsKey(THROWABLE_KEY)) {
      throw localContext.get(THROWABLE_KEY);
    }
    return result;
  }
}
