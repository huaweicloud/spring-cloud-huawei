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

import java.io.IOException;

import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedFunction0;

public class GovernanceClientHttpRequestInterceptor implements ClientHttpRequestInterceptor {

  @Autowired
  private RetryHandler retryHandler;

  @Autowired(required = false)
  private ClientRecoverPolicy<Object> clientRecoverPolicy;

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    GovernanceRequest governanceRequest = convert(request);

    CheckedFunction0<ClientHttpResponse> next = () -> execution.execute(request, body);
    DecorateCheckedSupplier<ClientHttpResponse> dcs = Decorators.ofCheckedSupplier(next);

    try {
      SpringCloudInvocationContext.setInvocationContext();

      addRetry(dcs, governanceRequest);

      return dcs.get();
    } catch (Throwable e) {
      if (clientRecoverPolicy != null) {
        return (ClientHttpResponse) clientRecoverPolicy.apply(e);
      }
      throw new RuntimeException(e);
    } finally {
      SpringCloudInvocationContext.removeInvocationContext();
    }
  }

  private GovernanceRequest convert(HttpRequest request) {
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setUri(request.getURI().getPath());
    governanceRequest.setMethod(request.getMethod().name());
    governanceRequest.setHeaders(request.getHeaders().toSingleValueMap());
    return governanceRequest;
  }

  private void addRetry(DecorateCheckedSupplier<ClientHttpResponse> dcs, GovernanceRequest request) {
    Retry retry = retryHandler.getActuator(request);
    if (retry != null) {
      dcs.withRetry(retry);
    }
  }
}
