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
package com.huaweicloud.servicecomb.discovery.discovery;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

import reactor.core.publisher.Flux;
import reactor.core.scheduler.Schedulers;

public class ServiceCombReactiveDiscoveryClient implements ReactiveDiscoveryClient {

  private DiscoveryClient discoveryClient;

  public ServiceCombReactiveDiscoveryClient(DiscoveryClient discoveryClient) {
    this.discoveryClient = discoveryClient;
  }

  @Override
  public String description() {
    return "SerivceComb Reactive Discovery";
  }

  @Override
  public Flux<ServiceInstance> getInstances(String serviceId) {
    return Flux.defer(() -> Flux.fromIterable(discoveryClient.getInstances(serviceId)))
        .subscribeOn(Schedulers.boundedElastic());
  }

  @Override
  public Flux<String> getServices() {
    return Flux.defer(() -> Flux.fromIterable(discoveryClient.getServices()))
        .subscribeOn(Schedulers.boundedElastic());
  }
}
