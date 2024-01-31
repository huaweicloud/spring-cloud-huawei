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

package com.huaweicloud.governance.adapters.loadbalancer.weightedResponseTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.NoopServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;

import reactor.core.publisher.Mono;

/**
 * Rule based on response time.
 */
public class WeightedResponseTimeLoadBalancer implements ReactorServiceInstanceLoadBalancer {
  private static final Logger LOGGER = LoggerFactory.getLogger(WeightedResponseTimeLoadBalancer.class);

  // when all servers are very fast(less than MIN_GAP), use round-robin rule.
  private static final double MIN_GAP = 10d;

  // calculate stats once per RANDOM_PERCENT requests.
  private static final int RANDOM_PERCENT = 1000;

  private final Object lock = new Object();

  private final AtomicInteger counter = new AtomicInteger(0);

  // notices: rule will always use as a fixed group of instance, see LoadBalancer for details.
  private volatile int size = -1;

  private volatile List<Double> cacheStates = new ArrayList<>();

  final String serviceId;

  ObjectProvider<ServiceInstanceListSupplier> supplier;

  final AtomicInteger position;

  public WeightedResponseTimeLoadBalancer(String serviceId, int position,
      ObjectProvider<ServiceInstanceListSupplier> serviceInstanceListSupplierProvider) {
    this.serviceId = serviceId;
    this.supplier = serviceInstanceListSupplierProvider;
    this.position = new AtomicInteger(position);
  }

  @SuppressWarnings("rawtypes")
  @Override
  public Mono<Response<ServiceInstance>> choose(Request request) {
    ServiceInstanceListSupplier supplier = this.supplier.getIfAvailable(NoopServiceInstanceListSupplier::new);
    return supplier.get(request).next().map((serviceInstances) -> this.buildInstanceResponse(supplier,
        serviceInstances));
  }

  private Response<ServiceInstance> buildInstanceResponse(ServiceInstanceListSupplier supplier,
      List<ServiceInstance> serviceInstances) {
    Response<ServiceInstance> serviceInstanceResponse = getWeightedResponesTimeInstance(serviceInstances);
    if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
      ((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
    }
    return serviceInstanceResponse;
  }

  private Response<ServiceInstance> getWeightedResponesTimeInstance(List<ServiceInstance> instances) {
    int count = counter.getAndIncrement();
    if (count % RANDOM_PERCENT == 0 || size != instances.size()) {
      synchronized (lock) {
        this.cacheStates = doCalculateTotalWeights(instances);
        this.size = instances.size();
      }
    }
    List<Double> stats = this.cacheStates;
    if (!stats.isEmpty()) {
      double finalTotal = stats.get(stats.size() - 1);
      List<Double> weights = new ArrayList<>(instances.size());
      for (int i = 0; i < stats.size() - 1; i++) {
        weights.add(finalTotal - stats.get(i));
      }
      double ran = ThreadLocalRandom.current().nextDouble() * finalTotal * (instances.size() - 1);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current weights: {}, finalTotal random: {}", weights, ran);
      }
      for (int i = 0; i < weights.size(); i++) {
        ran -= weights.get(i);
        if (ran < 0) {
          return new DefaultResponse(instances.get(i));
        }
      }
      return new DefaultResponse(instances.get(instances.size() - 1));
    }
    return getRoundRobinInstance(instances);
  }

  private static List<Double> doCalculateTotalWeights(List<ServiceInstance> instances) {
    List<Double> stats = new ArrayList<>(instances.size() + 1);
    double totalWeights = 0;
    boolean needRandom = false;
    for (ServiceInstance instance : instances) {
      // this method will create new instance, so we cache the states.
      double avgTime = ServiceInstanceMetrics.getMetrics(instance).getSnapshot().getAverageDuration().toMillis();
      if (!needRandom && avgTime > MIN_GAP) {
        needRandom = true;
      }
      totalWeights += avgTime;
      stats.add(avgTime);
    }
    stats.add(totalWeights);
    if (needRandom) {
      return stats;
    } else {
      return new ArrayList<>();
    }
  }

  private Response<ServiceInstance> getRoundRobinInstance(List<ServiceInstance> instances) {
    if (instances.isEmpty()) {
      if (LOGGER.isWarnEnabled()) {
        LOGGER.warn("No servers available for service: " + serviceId);
      }
      return new EmptyResponse();
    }
    // Do not move position when there is only 1 instance, especially some suppliers
    // have already filtered instances
    if (instances.size() == 1) {
      return new DefaultResponse(instances.get(0));
    }
    int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;
    ServiceInstance instance = instances.get(pos % instances.size());
    return new DefaultResponse(instance);
  }
}
