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

import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huaweicloud.common.adapters.web.FallbackClientHttpResponse;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.vavr.CheckedFunction0;

public class BulkheadClientHttpRequestInterceptor implements ClientHttpRequestInterceptor, Ordered {
  private static final Logger LOG = LoggerFactory.getLogger(IsolationClientHttpRequestInterceptor.class);

  private static final int ORDER = 200;

  private final InstanceBulkheadHandler instanceBulkheadHandler;

  public BulkheadClientHttpRequestInterceptor(InstanceBulkheadHandler instanceBulkheadHandler) {
    this.instanceBulkheadHandler = instanceBulkheadHandler;
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) {
    GovernanceRequestExtractor governanceRequest = RestTemplateUtils.createGovernanceRequest(request);
    try {
      Bulkhead bulkhead = instanceBulkheadHandler.getActuator(governanceRequest);
      if (bulkhead == null) {
        return execution.execute(request, body);
      }
      CheckedFunction0<ClientHttpResponse> next = () -> execution.execute(request, body);
      DecorateCheckedSupplier<ClientHttpResponse> dcs = Decorators.ofCheckedSupplier(next);
      dcs.withBulkhead(bulkhead);
      return dcs.get();
    } catch (Throwable e) {
      if (e instanceof BulkheadFullException) {
        // when instance isolated, request to pull instances.
        LOG.warn("instance bulkhead is full [{}]", governanceRequest.instanceId());
        return new FallbackClientHttpResponse(503, "instance bulkhead is full.");
      }
      throw new RuntimeException(e);
    }
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
