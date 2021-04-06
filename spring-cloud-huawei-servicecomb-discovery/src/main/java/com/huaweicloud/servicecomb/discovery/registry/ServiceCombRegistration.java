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

package com.huaweicloud.servicecomb.discovery.registry;

import java.net.URI;
import java.util.Map;

import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.huaweicloud.servicecomb.discovery.discovery.MicroserviceHandler;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

/**
 * Spring Cloud Registration 实现。
 *
 * Spring Cloud Registration的注册过程要求 serviceId 是预先分配好的，service center的注册过程 serviceId 是注册成功
 * 分配的， 两个过程不一样。因此 Registration 只是一个空的实现， 不能够使用， 相关信息在 ServiceRegistry 的实现里面提供。
 */
public class ServiceCombRegistration implements Registration {

  private Microservice microservice;

  private MicroserviceInstance microserviceInstance;

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  public ServiceCombRegistration(ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      TagsProperties tagsProperties) {
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
    this.microservice = MicroserviceHandler.createMicroservice(serviceCombDiscoveryProperties);
    this.microserviceInstance = MicroserviceHandler
        .createMicroserviceInstance(serviceCombDiscoveryProperties, tagsProperties);
  }

  public ServiceCombDiscoveryProperties getServiceCombDiscoveryProperties() {
    return this.serviceCombDiscoveryProperties;
  }

  public MicroserviceInstance getMicroserviceInstance() {
    return microserviceInstance;
  }

  public Microservice getMicroservice() {
    return microservice;
  }

  @Override
  public String getServiceId() {
    throw new IllegalStateException("not supported");
  }

  @Override
  public String getHost() {
    throw new IllegalStateException("not supported");
  }

  @Override
  public int getPort() {
    throw new IllegalStateException("not supported");
  }

  @Override
  public boolean isSecure() {
    throw new IllegalStateException("not supported");
  }

  @Override
  public URI getUri() {
    throw new IllegalStateException("not supported");
  }

  @Override
  public Map<String, String> getMetadata() {
    throw new IllegalStateException("not supported");
  }
}
