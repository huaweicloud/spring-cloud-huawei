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

package com.huaweicloud.governance.adapters.web;

import java.io.IOException;
import java.time.Duration;

import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huaweicloud.common.adapters.web.FallbackClientHttpResponse;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.governance.event.InstanceIsolatedEvent;

import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.vavr.CheckedFunction0;

public class IsolationClientHttpRequestInterceptor implements ClientHttpRequestInterceptor, Ordered {
  private static final Logger LOG = LoggerFactory.getLogger(IsolationClientHttpRequestInterceptor.class);

  private static final int ORDER = 100;

  private final InstanceIsolationHandler instanceIsolationHandler;

  public IsolationClientHttpRequestInterceptor(InstanceIsolationHandler instanceIsolationHandler) {
    this.instanceIsolationHandler = instanceIsolationHandler;
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    GovernanceRequestExtractor governanceRequest = RestTemplateUtils.createGovernanceRequest(request);

    CircuitBreakerPolicy circuitBreakerPolicy = instanceIsolationHandler.matchPolicy(governanceRequest);
    if (circuitBreakerPolicy != null && circuitBreakerPolicy.isForceOpen()) {
      return new FallbackClientHttpResponse(503,
          "Policy " + circuitBreakerPolicy.getName() + " forced open and deny requests");
    }

    if (circuitBreakerPolicy != null && !circuitBreakerPolicy.isForceClosed()) {
      CircuitBreaker circuitBreaker = instanceIsolationHandler.getActuator(governanceRequest);
      if (circuitBreaker != null) {
        CheckedFunction0<ClientHttpResponse> next = () -> execution.execute(request, body);
        DecorateCheckedSupplier<ClientHttpResponse> dcs = Decorators.ofCheckedSupplier(next);
        dcs.withCircuitBreaker(circuitBreaker);
        try {
          return dcs.get();
        } catch (Throwable e) {
          if (e instanceof CallNotPermittedException) {
            LOG.error("instance isolated [{}], [{}]", governanceRequest.instanceId(), e.getMessage());
            EventManager.post(new InstanceIsolatedEvent(governanceRequest.instanceId(),
                Duration.parse(circuitBreakerPolicy.getWaitDurationInOpenState())));
            return new FallbackClientHttpResponse(503, "instance isolated");
          }
          if (e instanceof RuntimeException) {
            throw (RuntimeException) e;
          }
          throw new RuntimeException(e);
        }
      }
    }
    return execution.execute(request, body);
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
