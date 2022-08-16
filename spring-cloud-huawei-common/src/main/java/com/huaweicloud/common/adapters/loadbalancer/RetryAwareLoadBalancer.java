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

package com.huaweicloud.common.adapters.loadbalancer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.governance.handler.LoadBalanceHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.loadbanlance.LoadBalance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import com.huaweicloud.common.configration.dynamic.LoadBalancerProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

/**
 * load balancers to support retry on same and on next
 */
public class RetryAwareLoadBalancer implements ReactorServiceInstanceLoadBalancer {
  private static final Logger LOGGER = LoggerFactory.getLogger(RetryAwareLoadBalancer.class);

  private final String serviceId;

  private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

  private final LoadBalancerProperties loadBalancerProperties;

  private final LoadBalanceHandler loadBalanceHandler;

  private final Map<String, ReactorServiceInstanceLoadBalancer> loadBalancers = new ConcurrentHashMap<>();

  public RetryAwareLoadBalancer(ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider,
      String serviceId, LoadBalancerProperties loadBalancerProperties, LoadBalanceHandler loadBalanceHandler) {
    this.serviceInstanceListSupplierProvider = serviceInstanceListSupplierProvider;
    this.serviceId = serviceId;
    this.loadBalancerProperties = loadBalancerProperties;
    this.loadBalanceHandler = loadBalanceHandler;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Mono<Response<ServiceInstance>> choose(Request request) {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    GovernanceRequest governanceRequest = convert(request);
    LoadBalance loadBalance = loadBalanceHandler.getActuator(governanceRequest);
    if (loadBalance != null) {
      loadBalancerProperties.setRule(loadBalance.getRule());
    }
    if (context.getLocalContext(RetryContext.RETRY_CONTEXT) == null) {
      // gateway do not use RetryContext
      ReactorServiceInstanceLoadBalancer loadBalancer = loadBalancers.computeIfAbsent(loadBalancerProperties.getRule(),
          key -> {
            if (LoadBalancerProperties.RULE_RANDOM.equals(key)) {
              return new RandomLoadBalancer(this.serviceInstanceListSupplierProvider, this.serviceId);
            } else {
              return new RoundRobinLoadBalancer(this.serviceInstanceListSupplierProvider, this.serviceId);
            }
          });
      return loadBalancer.choose(request);
    }

    // feign / restTemplate using RetryContext
    RetryContext retryContext = context.getLocalContext(RetryContext.RETRY_CONTEXT);
    if (retryContext.trySameServer() && retryContext.getLastServer() != null) {
      retryContext.incrementRetry();
      return Mono.just(new DefaultResponse(retryContext.getLastServer()));
    }

    ReactorServiceInstanceLoadBalancer loadBalancer = loadBalancers.computeIfAbsent(loadBalancerProperties.getRule(),
        key -> {
          if (LoadBalancerProperties.RULE_RANDOM.equals(key)) {
            return new RandomLoadBalancer(this.serviceInstanceListSupplierProvider, this.serviceId);
          } else {
            return new RoundRobinLoadBalancer(this.serviceInstanceListSupplierProvider, this.serviceId);
          }
        });
    return loadBalancer.choose(request).doOnSuccess(r -> retryContext.setLastServer(r.getServer()));
  }

  @SuppressWarnings("rawtypes")
  private GovernanceRequest convert(Request request) {
    GovernanceRequest governanceRequest = new GovernanceRequest();
    Object context = request.getContext();
    if (context instanceof DefaultRequestContext) {
      Object clientRequest = ((DefaultRequestContext) context).getClientRequest();
      if (clientRequest instanceof RequestData) {
        RequestData requestData = (RequestData) clientRequest;
        governanceRequest.setUri(requestData.getUrl().getPath());
        governanceRequest.setMethod(requestData.getHttpMethod().name());
        governanceRequest.setHeaders(requestData.getHeaders().toSingleValueMap());
        governanceRequest.setServiceName(serviceId);
      } else if (clientRequest instanceof DecorateLoadBalancerRequest) {
        DecorateLoadBalancerRequest requestData = (DecorateLoadBalancerRequest) clientRequest;
        governanceRequest.setUri((requestData.getRequest().getURI().getPath()));
        governanceRequest.setMethod(requestData.getRequest().getMethod().name());
        governanceRequest.setHeaders(
            requestData.getRequest().getHeaders().toSingleValueMap());
        String serviceName = requestData.getRequest().getURI().getHost();
        governanceRequest.setServiceName(serviceName);
      } else {
        LOGGER.warn("not implemented client request {}.", clientRequest == null ? null : clientRequest.getClass());
      }
    } else {
      LOGGER.warn("not implemented context {}.", context == null ? null : context.getClass());
    }
    return governanceRequest;
  }
}
