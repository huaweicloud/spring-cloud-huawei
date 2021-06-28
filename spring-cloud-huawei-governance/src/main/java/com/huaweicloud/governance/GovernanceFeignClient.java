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

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedFunction0;
import feign.Request;
import feign.Response;

@Aspect
public class GovernanceFeignClient {
  @Autowired
  private RetryHandler retryHandler;

  @Autowired(required = false)
  private ClientRecoverPolicy<Object> clientRecoverPolicy;

  @Pointcut("execution(* org.springframework.cloud.openfeign.ribbon.LoadBalancerFeignClient.execute(..))")
  public void pointCut() {
  }

  @Around("pointCut()")
  public Object aroundInvoke(ProceedingJoinPoint pjp) throws Throwable {

    Request request = (Request) pjp.getArgs()[0];

    GovernanceRequest governanceRequest = convert(request);

    CheckedFunction0<Object> next = pjp::proceed;

    DecorateCheckedSupplier<Object> dcs = Decorators.ofCheckedSupplier(next);

    try {
      SpringCloudInvocationContext.setInvocationContext();

      addRetry(dcs, governanceRequest);

      return dcs.get();
    } catch (Throwable e) {
      if (clientRecoverPolicy != null) {
        return (Response) clientRecoverPolicy.apply(e);
      }
      throw new RuntimeException(e);
    } finally {
      SpringCloudInvocationContext.removeInvocationContext();
    }
  }

  private GovernanceRequest convert(Request request) {
    GovernanceRequest governanceRequest = new GovernanceRequest();
    try {
      URL url = new URL(request.url());
      governanceRequest.setUri(url.getPath());
      governanceRequest.setMethod(request.method());
      Map<String, String> headers = new HashMap<>(request.headers().size());
      request.headers().forEach((k, v) -> headers.put(k, v.iterator().next()));
      governanceRequest.setHeaders(headers);
      return governanceRequest;
    } catch (MalformedURLException e) {
      return governanceRequest;
    }
  }

  private void addRetry(DecorateCheckedSupplier<Object> dcs, GovernanceRequest request) {
    Retry retry = retryHandler.getActuator(request);
    if (retry != null) {
      dcs.withRetry(retry);
    }
  }
}
