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
import java.net.URI;

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.processor.injection.Fault;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators.FaultInjectionDecorateCheckedSupplier;
import org.apache.servicecomb.http.client.common.HttpUtils;
import org.springframework.http.HttpMethod;
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
import com.huaweicloud.governance.adapters.loadbalancer.RetryContext;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedFunction0;

/**
 * Add retry, fault injection to RestTemplate
 *
 * NOTICE: doExecute is copied from RestTemplate. Need update when upgrading.
 */
public class GovernanceRestTemplate extends RestTemplate {
  private static final String CONTEXT_IS_RETRY = "x-is-retry";

  private static final String CONTEXT_LAST_RESPONSE = "x-last-response";

  private final Object faultObject = new Object();

  private final RetryHandler retryHandler;

  private final FaultInjectionHandler faultInjectionHandler;

  public GovernanceRestTemplate(RetryHandler retryHandler,
      FaultInjectionHandler faultInjectionHandler) {
    this.retryHandler = retryHandler;
    this.faultInjectionHandler = faultInjectionHandler;
  }

  @SuppressWarnings("PMD.UseTryWithResources")
  @Nullable
  @Override
  protected <T> T doExecute(URI url, @Nullable String uriTemplate, @Nullable HttpMethod method, @Nullable RequestCallback requestCallback,
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
      response = executeWithFault(url, method, requestCallback, request);
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

  private ClientHttpResponse executeWithFault(URI url, @Nullable HttpMethod method,
      @Nullable RequestCallback requestCallback,
      ClientHttpRequest request) {
    GovernanceRequestExtractor governanceRequest = RestTemplateUtils.createGovernanceRequest(request);

    try {
      FallbackClientHttpResponse result = addFault(governanceRequest);
      if (result != null) {
        return result;
      }

      return executeWithRetry(url, method, requestCallback, request, governanceRequest);
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private ClientHttpResponse executeWithRetry(URI url, HttpMethod method, RequestCallback requestCallback,
      ClientHttpRequest request, GovernanceRequestExtractor governanceRequest) {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();

    try {
      Retry retry = retryHandler.getActuator(governanceRequest);
      if (retry == null) {
        // when retry not enabled and Isolation enabled, we need get instance from RetryContext
        RetryContext retryContext = new RetryContext(0);
        context.putLocalContext(RetryContext.RETRY_CONTEXT, retryContext);
        return request.execute();
      }

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
          // close last response in retry
          ClientHttpResponse lastResponse = context.getLocalContext(CONTEXT_LAST_RESPONSE);
          if (lastResponse != null) {
            lastResponse.close();
          }
        }
        ClientHttpResponse response = execution.execute();
        context.putLocalContext(CONTEXT_LAST_RESPONSE, response);
        return response;
      };

      DecorateCheckedSupplier<ClientHttpResponse> dcs = Decorators.ofCheckedSupplier(next);
      dcs.withRetry(retry);
      RetryPolicy retryPolicy = retryHandler.matchPolicy(governanceRequest);
      RetryContext retryContext = new RetryContext(retryPolicy.getRetryOnSame());
      context.putLocalContext(RetryContext.RETRY_CONTEXT, retryContext);
      return dcs.get();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private FallbackClientHttpResponse addFault(GovernanceRequestExtractor governanceRequest) {
    Fault fault = faultInjectionHandler.getActuator(governanceRequest);
    if (fault != null) {
      FaultInjectionDecorateCheckedSupplier<Object> faultInjectionDecorateCheckedSupplier =
          FaultInjectionDecorators.ofCheckedSupplier(() -> faultObject);
      faultInjectionDecorateCheckedSupplier.withFaultInjection(fault);
      try {
        Object result = faultInjectionDecorateCheckedSupplier.get();
        if (result != faultObject) {
          if (result == null) {
            return new FallbackClientHttpResponse(200);
          }
          return new FallbackClientHttpResponse(200, HttpUtils.serialize(result), "application/json");
        }
      } catch (Throwable e) {
        return new FallbackClientHttpResponse(500, e.getMessage());
      }
    }
    return null;
  }
}
