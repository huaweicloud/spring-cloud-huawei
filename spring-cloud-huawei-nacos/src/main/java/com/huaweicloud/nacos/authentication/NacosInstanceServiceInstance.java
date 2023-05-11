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

package com.huaweicloud.nacos.authentication;

import java.net.URI;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceInstance;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.huaweicloud.common.disovery.InstanceIDAdapter;
import com.huaweicloud.governance.authentication.Const;
import com.huaweicloud.governance.authentication.GovernaceServiceInstance;

public class NacosInstanceServiceInstance implements GovernaceServiceInstance, ServiceInstance {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosInstanceServiceInstance.class);

  private final NacosServiceInstance nacosServiceInstance;

  private final NacosDiscoveryProperties properties;

  private final NacosServiceDiscovery serviceDiscovery;

  private final NacosRegistration registration;

  public NacosInstanceServiceInstance(NacosServiceInstance nacosServiceInstance,
      NacosDiscoveryProperties properties, NacosRegistration registration,
      NacosServiceDiscovery serviceDiscovery) {
    this.nacosServiceInstance = nacosServiceInstance;
    this.properties = properties;
    this.registration = registration;
    this.serviceDiscovery = serviceDiscovery;
  }

  @Override
  public void setPublickey(String publicKeyEncoded) {
    properties.getMetadata().put(Const.INSTANCE_PUBKEY_PRO, publicKeyEncoded);
  }

  @Override
  public String getInstanceId() {
    return InstanceIDAdapter.instanceId(registration);
  }

  @Override
  public String getServiceId() {
    return registration.getServiceId();
  }

  @Override
  public String getHost() {
    //not implement
    return null;
  }

  @Override
  public int getPort() {
    //not implement
    return 0;
  }

  @Override
  public boolean isSecure() {
    //not implement
    return false;
  }

  @Override
  public URI getUri() {
    //not implement
    return null;
  }

  @Override
  public Map<String, String> getMetadata() {
    //not implement
    return nacosServiceInstance.getMetadata();
  }

  @Override
  public String getPublicKeyFromInstance(String instanceId, String serviceId) {
    return getPropertyValue(serviceId, instanceId, Const.INSTANCE_PUBKEY_PRO);
  }

  @Override
  public String getPropertyValue(String serviceId, String instanceId, String propertyName) {
    ServiceInstance serviceInstance = getMicroservice(serviceId, instanceId);
    if (instances != null) {
      if ("serviceName".equals(propertyName)) {
        return serviceInstance.getServiceId();
      }
      return serviceInstance.getMetadata().get(propertyName);
    } else {
      LOGGER.error("not instance found {}-{}, maybe attack", instanceId, serviceId);
      return "";
    }
  }

  private ServiceInstance getMicroservice(String serviceId, String instanceId) {
    try {
      String key = String.format("%s@%s", serviceId, instanceId);
      return instances.get(key, () -> {
        ServiceInstance serviceInstance = serviceDiscovery.getInstances(serviceId)
            .stream()
            .filter(instance -> InstanceIDAdapter.instanceId(instance).equals(instanceId))
            .findAny()
            .get();
        if (serviceInstance == null) {
          throw new IllegalArgumentException("service id not exists.");
        }
        return serviceInstance;
      });
    } catch (ExecutionException | UncheckedExecutionException e) {
      LOGGER.error("get microservice instance from nacos failed, {}, {}",
          String.format("%s@%s", serviceId, instanceId),
          e.getMessage());
      return null;
    }
  }

  private static final Cache<String, ServiceInstance> instances = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  @Override
  public String getVersion(ServiceInstance serviceInstance) {
    return serviceInstance.getMetadata().getOrDefault("version", "0.0.1");
  }

  @Override
  public String getServiceName(ServiceInstance serviceInstance) {
    return serviceInstance.getServiceId();
  }

  @Override
  public Map<String, String> getProperties(ServiceInstance serviceInstance) {
    return serviceInstance.getMetadata();
  }

  @Override
  public String getAvailableZone() {
    //not implement
    return null;
  }

  @Override
  public String getRegion() {
    //not implement
    return null;
  }
}
