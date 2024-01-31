/*
 * Copyright 2013-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.governance.adapters.feign;

import java.io.IOException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.policy.CircuitBreakerPolicy;
import org.apache.servicecomb.governance.policy.RetryPolicy;
import org.apache.servicecomb.governance.processor.injection.Fault;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators.FaultInjectionDecorateCheckedSupplier;
import org.apache.servicecomb.http.client.common.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.CompletionContext;
import org.springframework.cloud.client.loadbalancer.DefaultRequest;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycle;
import org.springframework.cloud.client.loadbalancer.LoadBalancerLifecycleValidator;
import org.springframework.cloud.client.loadbalancer.LoadBalancerProperties;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.ResponseData;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.loadbalancer.LoadBalancerFeignRequestTransformer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.util.Assert;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.context.InvocationStage;
import com.huaweicloud.common.disovery.InstanceIDAdapter;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.governance.GovernanceConst;
import com.huaweicloud.governance.adapters.loadbalancer.RetryContext;
import com.huaweicloud.governance.adapters.loadbalancer.weightedResponseTime.ServiceInstanceMetrics;
import com.huaweicloud.governance.event.InstanceIsolatedEvent;

import io.github.resilience4j.bulkhead.Bulkhead;
import io.github.resilience4j.bulkhead.BulkheadFullException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.core.metrics.Metrics.Outcome;
import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.vavr.CheckedFunction0;
import feign.Client;
import feign.Request;
import feign.Request.Options;
import feign.Response;

/**
 * Support for retry, fault injection and instance isolation, metrics for FeignBlockingLoadBalancerClient.
 *
 * NOTICE: this class is copied from
 *    #org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient
 *   and may need change when upgrading.
 */

@SuppressWarnings({"rawtypes", "unchecked", "deprecation"})
public class GovernanceFeignBlockingLoadBalancerClient implements Client {

  private static final Logger LOG = LoggerFactory.getLogger(GovernanceFeignBlockingLoadBalancerClient.class);

  private static final String CONTEXT_IS_RETRY = "x-is-retry";

  private static final String CONTEXT_LAST_RESPONSE = "x-last-response";

  private final Object faultObject = new Object();

  private final Client delegate;

  private final LoadBalancerClient loadBalancerClient;

  private final LoadBalancerClientFactory loadBalancerClientFactory;

  private final RetryHandler retryHandler;

  private final FaultInjectionHandler faultInjectionHandler;

  private final InstanceIsolationHandler instanceIsolationHandler;

  private final InstanceBulkheadHandler instanceBulkheadHandler;

  private final List<LoadBalancerFeignRequestTransformer> transformers;

  public GovernanceFeignBlockingLoadBalancerClient(RetryHandler retryHandler,
      FaultInjectionHandler faultInjectionHandler,
      InstanceIsolationHandler instanceIsolationHandler,
      InstanceBulkheadHandler instanceBulkheadHandler,
      Client delegate, LoadBalancerClient loadBalancerClient,
      LoadBalancerClientFactory loadBalancerClientFactory,
      List<LoadBalancerFeignRequestTransformer> transformers) {
    this.retryHandler = retryHandler;
    this.faultInjectionHandler = faultInjectionHandler;
    this.instanceIsolationHandler = instanceIsolationHandler;
    this.instanceBulkheadHandler = instanceBulkheadHandler;
    this.delegate = delegate;
    this.loadBalancerClient = loadBalancerClient;
    this.loadBalancerClientFactory = loadBalancerClientFactory;
    this.transformers = transformers;
  }

  @Override
  public Response execute(Request request, Request.Options options) {
    final URI originalUri = URI.create(request.url());

    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    String stageName = context.getInvocationStage().recordStageBegin(stageId(originalUri, request));

    long time = System.currentTimeMillis();
    try {
      Response response = decorateWithFault(request, options, originalUri);
      context.getInvocationStage().recordStageEnd(stageName);
      if (response.status() == 200) {
        metricsRecord(Outcome.SUCCESS, context, time);
      } else {
        metricsRecord(Outcome.ERROR, context, time);
      }
      return response;
    } catch (Throwable error) {
      context.getInvocationStage().recordStageEnd(stageName);
      metricsRecord(Outcome.ERROR, context, time);
      throw error;
    }
  }

  private void metricsRecord(Outcome outcome, InvocationContext context, long time) {
    if (context.getLocalContext(GovernanceConst.CONTEXT_CURRENT_INSTANCE) != null) {
      ServiceInstanceMetrics.getMetrics(context.getLocalContext(GovernanceConst.CONTEXT_CURRENT_INSTANCE))
          .record((System.currentTimeMillis() - time), TimeUnit.MILLISECONDS, outcome);
    }
  }

  private String stageId(URI uri, Request request) {
    return InvocationStage.STAGE_FEIGN + " " + request.httpMethod().name() + " " + uri.getPath();
  }

  private Response decorateWithFault(Request request, Options options, URI originalUri) {
    // add Fault
    GovernanceRequestExtractor governanceRequest = FeignUtils.convert(request, originalUri, null);
    Fault fault = faultInjectionHandler.getActuator(governanceRequest);
    if (fault != null) {
      FaultInjectionDecorateCheckedSupplier<Object> faultInjectionDecorateCheckedSupplier =
          FaultInjectionDecorators.ofCheckedSupplier(() -> faultObject);
      faultInjectionDecorateCheckedSupplier.withFaultInjection(fault);
      try {
        Object result = faultInjectionDecorateCheckedSupplier.get();
        if (result != faultObject) {
          Map<String, Collection<String>> headers = new HashMap<>();
          headers.put("Content-Type", Arrays.asList("application/json"));
          if (result == null) {
            return Response.builder().status(200)
                .request(request)
                .headers(headers)
                .build();
          }
          return Response.builder().status(200)
              .request(request)
              .headers(headers)
              .body(HttpUtils.serialize(result).getBytes(
                  StandardCharsets.UTF_8)).build();
        }
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }

    return decorateWithRetry(request, options, originalUri, governanceRequest);
  }

  private Response decorateWithRetry(Request request, Options options, URI originalUri,
      GovernanceRequestExtractor governanceRequest) {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    Retry retry = retryHandler.getActuator(governanceRequest);
    if (retry == null) {
      // when retry not enabled and Isolation enabled, we need get instance from RetryContext
      RetryContext retryContext = new RetryContext(0);
      context.putLocalContext(RetryContext.RETRY_CONTEXT, retryContext);
      return doExecute(originalUri, request, options);
    }

    CheckedFunction0<Response> next = () -> {
      if (context.getLocalContext(CONTEXT_IS_RETRY) == null) {
        context.putLocalContext(CONTEXT_IS_RETRY, true);
      } else {
        // close last response in retry
        Response response = context.getLocalContext(CONTEXT_LAST_RESPONSE);
        if (response != null) {
          response.close();
        }
      }
      Response response = doExecute(originalUri, request, options);
      context.putLocalContext(CONTEXT_LAST_RESPONSE, response);
      return response;
    };

    DecorateCheckedSupplier<Response> dcs = Decorators.ofCheckedSupplier(next);

    try {
      dcs.withRetry(retry);

      addRetryContext(governanceRequest);

      return dcs.get();
    } catch (Throwable e) {
      throw new RuntimeException(e);
    }
  }

  private void addRetryContext(GovernanceRequestExtractor governanceRequest) {
    RetryPolicy retryPolicy = retryHandler.matchPolicy(governanceRequest);
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    RetryContext retryContext = new RetryContext(retryPolicy.getRetryOnSame());
    context.putLocalContext(RetryContext.RETRY_CONTEXT, retryContext);
  }

  private Response doExecute(URI originalUri, Request request, Options options) {
    String serviceId = originalUri.getHost();
    Assert.state(serviceId != null, "Request URI does not contain a valid hostname: " + originalUri);
    String hint = getHint(serviceId);
    DefaultRequest<RequestDataContext> lbRequest = new DefaultRequest<>(
        new RequestDataContext(buildRequestData(request), hint));
    Set<LoadBalancerLifecycle> supportedLifecycleProcessors = LoadBalancerLifecycleValidator
        .getSupportedLifecycleProcessors(
            loadBalancerClientFactory.getInstances(serviceId, LoadBalancerLifecycle.class),
            RequestDataContext.class, ResponseData.class, ServiceInstance.class);
    supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStart(lbRequest));
    ServiceInstance instance = loadBalancerClient.choose(serviceId, lbRequest);
    org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse = new DefaultResponse(
        instance);
    if (instance == null) {
      String message = "Load balancer does not contain an instance for the service " + serviceId;
      if (LOG.isWarnEnabled()) {
        LOG.warn(message);
      }
      supportedLifecycleProcessors.forEach(lifecycle -> lifecycle
          .onComplete(new CompletionContext<ResponseData, ServiceInstance, RequestDataContext>(
              CompletionContext.Status.DISCARD, lbRequest, lbResponse)));
      return Response.builder().request(request).status(HttpStatus.SERVICE_UNAVAILABLE.value())
          .body(message, StandardCharsets.UTF_8).build();
    }

    GovernanceRequestExtractor governanceRequest = FeignUtils.convert(request, originalUri,
        InstanceIDAdapter.instanceId(instance));

    String reconstructedUrl = loadBalancerClient.reconstructURI(instance, originalUri).toString();
    Request newRequest = buildRequest(request, reconstructedUrl, instance);
    return executeWithLoadBalancerLifecycleProcessing(governanceRequest, delegate, options, newRequest, lbRequest,
        lbResponse,
        supportedLifecycleProcessors);
  }

  protected Request buildRequest(Request request, String reconstructedUrl) {
    return Request.create(request.httpMethod(), reconstructedUrl, request.headers(), request.body(),
        request.charset(), request.requestTemplate());
  }

  protected Request buildRequest(Request request, String reconstructedUrl, ServiceInstance instance) {
    Request newRequest = buildRequest(request, reconstructedUrl);
    if (transformers != null) {
      for (LoadBalancerFeignRequestTransformer transformer : transformers) {
        newRequest = transformer.transformRequest(newRequest, instance);
      }
    }
    return newRequest;
  }

  // Visible for Sleuth instrumentation
  public Client getDelegate() {
    return delegate;
  }

  private String getHint(String serviceId) {
    LoadBalancerProperties properties = loadBalancerClientFactory.getProperties(serviceId);
    String defaultHint = properties.getHint().getOrDefault("default", "default");
    String hintPropertyValue = properties.getHint().get(serviceId);
    return hintPropertyValue != null ? hintPropertyValue : defaultHint;
  }

  static Response executeWithLoadBalancerLifecycleProcessing(Client feignClient, Request.Options options,
      Request feignRequest, org.springframework.cloud.client.loadbalancer.Request lbRequest,
      org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
      Set<LoadBalancerLifecycle> supportedLifecycleProcessors) throws IOException {
    return executeWithLoadBalancerLifecycleProcessing(feignClient, options, feignRequest, lbRequest, lbResponse,
        supportedLifecycleProcessors, true);
  }

  static Response executeWithLoadBalancerLifecycleProcessing(Client feignClient, Request.Options options,
      Request feignRequest, org.springframework.cloud.client.loadbalancer.Request lbRequest,
      org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
      Set<LoadBalancerLifecycle> supportedLifecycleProcessors, boolean loadBalanced) throws IOException {
    supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onStartRequest(lbRequest, lbResponse));
    try {
      Response response = feignClient.execute(feignRequest, options);
      if (loadBalanced) {
        supportedLifecycleProcessors.forEach(
            lifecycle -> lifecycle.onComplete(new CompletionContext<>(CompletionContext.Status.SUCCESS,
                lbRequest, lbResponse, buildResponseData(response))));
      }
      return response;
    } catch (Exception exception) {
      if (loadBalanced) {
        supportedLifecycleProcessors.forEach(lifecycle -> lifecycle.onComplete(
            new CompletionContext<>(CompletionContext.Status.FAILED, exception, lbRequest, lbResponse)));
      }
      throw exception;
    }
  }

  static ResponseData buildResponseData(Response response) {
    HttpHeaders responseHeaders = new HttpHeaders();
    response.headers().forEach((key, value) -> responseHeaders.put(key, new ArrayList<>(value)));
    return new ResponseData(HttpStatusCode.valueOf(response.status()), responseHeaders, null,
        buildRequestData(response.request()));
  }

  static RequestData buildRequestData(Request request) {
    HttpHeaders requestHeaders = new HttpHeaders();
    request.headers().forEach((key, value) -> requestHeaders.put(key, new ArrayList<>(value)));
    return new RequestData(HttpMethod.valueOf(request.httpMethod().name()), URI.create(request.url()),
        requestHeaders, null, new HashMap<>());
  }

  private Response executeWithLoadBalancerLifecycleProcessing(GovernanceRequestExtractor governanceRequest,
      Client feignClient,
      Request.Options options,
      Request feignRequest, org.springframework.cloud.client.loadbalancer.Request lbRequest,
      org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
      Set<LoadBalancerLifecycle> supportedLifecycleProcessors) {

    return executeWithInstanceIsolation(governanceRequest, feignClient, options, feignRequest, lbRequest, lbResponse,
        supportedLifecycleProcessors);
  }

  private Response executeWithInstanceIsolation(GovernanceRequestExtractor governanceRequest, Client feignClient,
      Options options,
      Request feignRequest, org.springframework.cloud.client.loadbalancer.Request lbRequest,
      org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
      Set<LoadBalancerLifecycle> supportedLifecycleProcessors) {

    CircuitBreakerPolicy circuitBreakerPolicy = instanceIsolationHandler.matchPolicy(governanceRequest);
    if (circuitBreakerPolicy != null && circuitBreakerPolicy.isForceOpen()) {
      return Response.builder().status(503)
          .reason("Policy " + circuitBreakerPolicy.getName() + " forced open and deny requests").request(feignRequest)
          .build();
    }

    if (circuitBreakerPolicy != null && !circuitBreakerPolicy.isForceClosed()) {
      CircuitBreaker circuitBreaker = instanceIsolationHandler.getActuator(governanceRequest);
      if (circuitBreaker == null) {
        return executeWithInstanceBulkhead(governanceRequest, feignClient, options, feignRequest, lbRequest,
            lbResponse, supportedLifecycleProcessors);
      }

      CheckedFunction0<Response> next = () -> executeWithInstanceBulkhead(governanceRequest, feignClient, options,
          feignRequest, lbRequest, lbResponse,
          supportedLifecycleProcessors);

      DecorateCheckedSupplier<Response> dcs = Decorators.ofCheckedSupplier(next);
      dcs.withCircuitBreaker(circuitBreaker);

      try {
        return dcs.get();
      } catch (Throwable e) {
        if (e instanceof CallNotPermittedException) {
          // when instance isolated, request to pull instances.
          LOG.error("instance isolated [{}], [{}]", governanceRequest.instanceId(), e.getMessage());
          EventManager.post(new InstanceIsolatedEvent(governanceRequest.instanceId(),
              Duration.parse(circuitBreakerPolicy.getWaitDurationInOpenState())));
          return Response.builder().status(503).reason("instance isolated.").request(feignRequest).build();
        }

        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        }
        throw new RuntimeException(e);
      }
    }

    return executeWithInstanceBulkhead(governanceRequest, feignClient, options, feignRequest, lbRequest,
        lbResponse, supportedLifecycleProcessors);
  }

  private Response executeWithInstanceBulkhead(GovernanceRequestExtractor governanceRequest, Client feignClient,
      Options options,
      Request feignRequest, org.springframework.cloud.client.loadbalancer.Request lbRequest,
      org.springframework.cloud.client.loadbalancer.Response<ServiceInstance> lbResponse,
      Set<LoadBalancerLifecycle> supportedLifecycleProcessors) {

    try {
      Bulkhead bulkhead = instanceBulkheadHandler.getActuator(governanceRequest);
      if (bulkhead == null) {
        return executeWithLoadBalancerLifecycleProcessing(feignClient, options,
            feignRequest, lbRequest, lbResponse,
            supportedLifecycleProcessors, true);
      }

      CheckedFunction0<Response> next = () -> executeWithLoadBalancerLifecycleProcessing(feignClient, options,
          feignRequest, lbRequest, lbResponse,
          supportedLifecycleProcessors, true);

      DecorateCheckedSupplier<Response> dcs = Decorators.ofCheckedSupplier(next);

      dcs.withBulkhead(bulkhead);
      return dcs.get();
    } catch (Throwable e) {
      if (e instanceof BulkheadFullException) {
        LOG.error("instance bulkhead is full [{}]", governanceRequest.instanceId());
        return Response.builder().status(503).reason("instance bulkhead is full.").request(feignRequest).build();
      }

      throw new RuntimeException(e);
    }
  }
}
