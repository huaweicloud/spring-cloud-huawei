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

package com.huaweicloud.governance.adapters.loadbalancer;

import java.security.SecureRandom;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.servicecomb.governance.handler.LoadBalanceHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.processor.loadbanlance.LoadBalance;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;
import org.springframework.cloud.client.loadbalancer.RequestDataContext;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.RandomLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.RoundRobinLoadBalancer;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.WeightedServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.core.env.Environment;

import com.huaweicloud.common.configration.dynamic.LoadBalancerProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

/**
 * load balancers to support retry on same and on next
 */
public class RetryAwareLoadBalancer implements ReactorServiceInstanceLoadBalancer {
  private final String serviceId;

  private final ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider;

  private final LoadBalancerProperties loadBalancerProperties;

  private final LoadBalanceHandler loadBalanceHandler;

  private final Map<String, ReactorServiceInstanceLoadBalancer> loadBalancers = new ConcurrentHashMap<>();

  private final LoadBalancerClientFactory loadBalancerClientFactory;

  private final Environment environment;

  private final SecureRandom secureRandom = new SecureRandom();

  public RetryAwareLoadBalancer(LoadBalancerProperties loadBalancerProperties, LoadBalanceHandler loadBalanceHandler,
      LoadBalancerClientFactory loadBalancerClientFactory, Environment environment) {
    this.serviceId = environment.getProperty(LoadBalancerClientFactory.PROPERTY_NAME);
    this.serviceInstanceListSupplierProvider =
        loadBalancerClientFactory.getLazyProvider(serviceId, ServiceInstanceListSupplier.class);
    this.loadBalancerProperties = loadBalancerProperties;
    this.loadBalanceHandler = loadBalanceHandler;
    this.loadBalancerClientFactory = loadBalancerClientFactory;
    this.environment = environment;
  }

  @Override
  @SuppressWarnings("rawtypes")
  public Mono<Response<ServiceInstance>> choose(Request request) {
    InvocationContext context = getOrCreateInvocationContext(request);
    GovernanceRequestExtractor governanceRequest = LoadBalancerUtils.convert(request, serviceId);
    LoadBalance loadBalance = loadBalanceHandler.getActuator(governanceRequest);
    String rule = loadBalance != null ? loadBalance.getRule() : loadBalancerProperties.getRule();
    if (context.getLocalContext(RetryContext.RETRY_CONTEXT) == null) {
      ReactorServiceInstanceLoadBalancer loadBalancer = loadBalancers.computeIfAbsent(rule,
          key -> {
            if (LoadBalancerProperties.RULE_RANDOM.equals(key)) {
              return new RandomLoadBalancer(this.serviceInstanceListSupplierProvider, this.serviceId);
            } else if (LoadBalancerProperties.RULE_WEIGHT.equals(key)){
              return new InstanceWeightedLoadBalancer(buildWeightedServiceInstanceSupplier(serviceInstanceListSupplierProvider,
                  loadBalancerClientFactory), this.serviceId, secureRandom.nextInt(1000));
            } else {
              return new RoundRobinLoadBalancer(this.serviceInstanceListSupplierProvider, this.serviceId);
            }
          });
      return loadBalancer.choose(request);
    }

    RetryContext retryContext = context.getLocalContext(RetryContext.RETRY_CONTEXT);
    if (retryContext.trySameServer() && retryContext.getLastServer() != null) {
      retryContext.incrementRetry();
      return Mono.just(new DefaultResponse(retryContext.getLastServer()));
    }

    ReactorServiceInstanceLoadBalancer loadBalancer = loadBalancers.computeIfAbsent(rule,
        key -> {
          if (LoadBalancerProperties.RULE_RANDOM.equals(key)) {
            return new RandomLoadBalancer(this.serviceInstanceListSupplierProvider, this.serviceId);
          } else if (LoadBalancerProperties.RULE_WEIGHT.equals(key)){
            return new InstanceWeightedLoadBalancer(buildWeightedServiceInstanceSupplier(serviceInstanceListSupplierProvider,
                loadBalancerClientFactory), this.serviceId, secureRandom.nextInt(1000));
          } else {
            return new RoundRobinLoadBalancer(this.serviceInstanceListSupplierProvider, this.serviceId);
          }
        });
    return loadBalancer.choose(request).doOnSuccess(r -> retryContext.setLastServer(r.getServer()));
  }

  private WeightedServiceInstanceListSupplier buildWeightedServiceInstanceSupplier(
      ObjectProvider<ServiceInstanceListSupplier> supplierProvider, LoadBalancerClientFactory factory) {
    if (environment.getProperty("spring.cloud.loadbalancer.weight-filter.enabled", boolean.class, true)) {
      factory.getProperties(serviceId).setCallGetWithRequestOnDelegates(true);
    }
    return new WeightedServiceInstanceListSupplier(
        supplierProvider.getIfAvailable(NoopServiceInstanceListSupplier::new), factory);
  }

  @SuppressWarnings("rawtypes")
  private InvocationContext getOrCreateInvocationContext(Request request) {
    Object context = request.getContext();
    if (context instanceof RequestDataContext) {
      RequestData data = ((RequestDataContext) context).getClientRequest();
      if (data.getAttributes().get(InvocationContextHolder.ATTRIBUTE_KEY) != null) {
        return (InvocationContext) data.getAttributes().get(InvocationContextHolder.ATTRIBUTE_KEY);
      }
    }
    return InvocationContextHolder.getOrCreateInvocationContext();
  }
}
