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

import java.io.IOException;
import java.net.URI;

import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequest;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.huaweicloud.common.adapters.web.FallbackClientHttpResponse;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.SpringCloudInvocationContext;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedFunction0;

/**
 * Retryable RestTemplate
 *
 * NOTICE: doExecute is copied from RestTemplate. Need update when upgrading.
 */
public class RetryableRestTemplate extends RestTemplate {
  private static final Logger LOGGER = LoggerFactory.getLogger(RetryableRestTemplate.class);

  private static final String CONTEXT_IS_RETRY = "x-is-retry";

  private final RetryHandler retryHandler;

  private final ClientRecoverPolicy<Object> clientRecoverPolicy;

  public RetryableRestTemplate(RetryHandler retryHandler, ClientRecoverPolicy<Object> clientRecoverPolicy) {
    this.retryHandler = retryHandler;
    this.clientRecoverPolicy = clientRecoverPolicy;
  }

  @SuppressWarnings("PMD.UseTryWithResources")
  @Nullable
  @Override
  protected <T> T doExecute(URI url, @Nullable HttpMethod method, @Nullable RequestCallback requestCallback,
      @Nullable ResponseExtractor<T> responseExtractor) throws RestClientException {

    Assert.notNull(url, "URI is required");
    Assert.notNull(method, "HttpMethod is required");
    ClientHttpResponse response = null;
    try {
      ClientHttpRequest request = createRequest(url, method);
      if (requestCallback != null) {
        requestCallback.doWithRequest(request);
      }

      // BEGIN: customize execution with retry
      response = executeWithRetry(url, method, requestCallback, request);
      // END: customize execution with retry

      handleResponse(url, method, response);
      return (responseExtractor != null ? responseExtractor.extractData(response) : null);
    } catch (IOException ex) {
      String resource = url.toString();
      String query = url.getRawQuery();
      resource = (query != null ? resource.substring(0, resource.indexOf('?')) : resource);
      throw new ResourceAccessException("I/O error on " + method.name() +
          " request for \"" + resource + "\": " + ex.getMessage(), ex);
    } finally {
      if (response != null) {
        response.close();
      }
    }
  }

  private ClientHttpResponse executeWithRetry(URI url, @Nullable HttpMethod method,
      @Nullable RequestCallback requestCallback,
      ClientHttpRequest request) {
    GovernanceRequest governanceRequest = convert(request);
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();

    CheckedFunction0<ClientHttpResponse> next = () -> {
      ClientHttpRequest execution = request;
      if (context.getLocalContext(CONTEXT_IS_RETRY) == null) {
        context.putLocalContext(CONTEXT_IS_RETRY, true);
      } else {
        // recreate request in retry
        execution = createRequest(url, method);
        if (requestCallback != null) {
          requestCallback.doWithRequest(execution);
        }
      }
      return execution.execute();
    };

    DecorateCheckedSupplier<ClientHttpResponse> dcs = Decorators.ofCheckedSupplier(next);

    try {
      SpringCloudInvocationContext.setInvocationContext();

      addRetry(dcs, governanceRequest);

      return dcs.get();
    } catch (Throwable e) {
      if (clientRecoverPolicy != null) {
        return (ClientHttpResponse) clientRecoverPolicy.apply(e);
      }
      LOGGER.error("retry catch throwable", e);
      // return 503, so that we can retry
      return new FallbackClientHttpResponse(500, e.getMessage());
    } finally {
      SpringCloudInvocationContext.removeInvocationContext();
    }
  }

  private GovernanceRequest convert(HttpRequest request) {
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setUri(request.getURI().getPath());
    governanceRequest.setMethod(request.getMethod().name());
    governanceRequest.setHeaders(request.getHeaders().toSingleValueMap());
    String serviceName = request.getURI().getHost();
    governanceRequest.setServiceName(serviceName);
    return governanceRequest;
  }

  private void addRetry(DecorateCheckedSupplier<ClientHttpResponse> dcs, GovernanceRequest request) {
    Retry retry = retryHandler.getActuator(request);
    if (retry != null) {
      dcs.withRetry(retry);
    }
  }
}
