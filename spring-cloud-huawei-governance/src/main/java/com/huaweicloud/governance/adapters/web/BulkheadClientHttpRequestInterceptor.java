/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huaweicloud.common.adapters.loadbalancer.RetryContext;
import com.huaweicloud.common.adapters.web.FallbackClientHttpResponse;
import com.huaweicloud.common.context.InvocationContextHolder;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.vavr.CheckedFunction0;

public class BulkheadClientHttpRequestInterceptor implements ClientHttpRequestInterceptor, Ordered {
  private static final Logger LOG = LoggerFactory.getLogger(IsolationClientHttpRequestInterceptor.class);

  private static final int ORDER = 200;

  private final InstanceBulkheadHandler instanceBulkheadHandler;

  private final ClientRecoverPolicy<ClientHttpResponse> clientRecoverPolicy;

  public BulkheadClientHttpRequestInterceptor(InstanceBulkheadHandler instanceBulkheadHandler,
      ClientRecoverPolicy<ClientHttpResponse> clientRecoverPolicy) {
    this.instanceBulkheadHandler = instanceBulkheadHandler;
    this.clientRecoverPolicy = clientRecoverPolicy;
  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) {
    CheckedFunction0<ClientHttpResponse> next = () -> execution.execute(request, body);

    DecorateCheckedSupplier<ClientHttpResponse> dcs = Decorators.ofCheckedSupplier(next);

    GovernanceRequest governanceRequest = convert(request);
    try {

      addInstanceBulkhead(dcs, governanceRequest);

      return dcs.get();
    } catch (Throwable e) {
      if (e instanceof BulkheadFullException) {
        // when instance isolated, request to pull instances.
        LOG.warn("instance bulkhead is full [{}]", governanceRequest.getInstanceId());
        return new FallbackClientHttpResponse(503, "instance bulkhead is full.");
      }
      if (clientRecoverPolicy != null) {
        return clientRecoverPolicy.apply(e);
      }
      LOG.error("instance bulkhead catch throwable", e);
      // return 503, so that we can retry
      return new FallbackClientHttpResponse(503, e.getMessage());
    }
  }

  private GovernanceRequest convert(HttpRequest request) {
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setUri(request.getURI().getPath());
    governanceRequest.setMethod(request.getMethod().name());
    governanceRequest.setHeaders(request.getHeaders().toSingleValueMap());

    RetryContext retryContext = InvocationContextHolder.getOrCreateInvocationContext()
        .getLocalContext(RetryContext.RETRY_CONTEXT);
    if (retryContext != null && retryContext.getLastServer() != null) {
      governanceRequest.setServiceName(retryContext.getLastServer().getServiceId());
      governanceRequest.setInstanceId(retryContext.getLastServer().getInstanceId());
    }
    return governanceRequest;
  }

  private void addInstanceBulkhead(DecorateCheckedSupplier<ClientHttpResponse> dcs,
      GovernanceRequest governanceRequest) {
    Bulkhead bulkhead = instanceBulkheadHandler.getActuator(governanceRequest);
    if (bulkhead != null) {
      dcs.withBulkhead(bulkhead);
    }
  }

  @Override
  public int getOrder() {
    return ORDER;
  }
}
