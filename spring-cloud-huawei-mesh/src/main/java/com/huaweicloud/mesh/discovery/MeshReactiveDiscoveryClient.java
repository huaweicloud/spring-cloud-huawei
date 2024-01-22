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

package com.huaweicloud.mesh.discovery;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

import reactor.core.publisher.Flux;

public class MeshReactiveDiscoveryClient implements ReactiveDiscoveryClient {
  private final MeshDiscoveryProperties meshDiscoveryProperties;

  public MeshReactiveDiscoveryClient(MeshDiscoveryProperties meshDiscoveryProperties) {
    this.meshDiscoveryProperties = meshDiscoveryProperties;
  }

  @Override
  public String description() {
    return "Client for docker or k8s";
  }

  @Override
  public Flux<ServiceInstance> getInstances(String serviceId) {
    return Flux.just(meshDiscoveryProperties.toServiceInstance(serviceId));
  }

  @Override
  public Flux<String> getServices() {
    // if
    // spring:
    //  cloud:
    //    gateway:
    //      discovery:
    //        locator:
    //          enabled: true
    // mush return all actual services. When this enabled in gateway,
    // can not use mesh discovery
    return Flux.empty();
  }

  @Override
  public int getOrder() {
    return meshDiscoveryProperties.getOrder();
  }
}
