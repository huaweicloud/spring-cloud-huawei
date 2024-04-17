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
package com.huaweicloud.nacos.discovery.authentication;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.huaweicloud.common.disovery.InstanceIDAdapter;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.GovernanceConst;
import com.huaweicloud.nacos.discovery.registry.NacosRegistration;

public class NacosAuthenticationAdapter implements AuthenticationAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(NacosAuthenticationAdapter.class);

  private static final Cache<String, ServiceInstance> INSTANCES_CACHE = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  public static final String SERVICE_NAME = "serviceName";

  private final DiscoveryClient serviceDiscovery;

  public NacosAuthenticationAdapter(DiscoveryClient serviceDiscovery) {
    this.serviceDiscovery = serviceDiscovery;
  }

  @Override
  public String getInstanceId(Registration registration) {
    return InstanceIDAdapter.instanceId(registration);
  }

  @Override
  public String getServiceId(Registration registration) {
    return registration.getServiceId();
  }

  @Override
  public void setPublicKey(Registration registration, String publicKey) {
    NacosRegistration nacosRegistration = (NacosRegistration) registration;
    nacosRegistration.getMetadata().put(GovernanceConst.INSTANCE_PUBKEY_PRO, publicKey);
  }

  @Override
  public String getPublicKeyFromInstance(String instanceId, String serviceId) {
    return getPropertyValue(serviceId, instanceId, GovernanceConst.INSTANCE_PUBKEY_PRO);
  }

  @Override
  public String getPropertyValue(String serviceId, String instanceId, String propertyName) {
    ServiceInstance serviceInstance = getServiceInstance(serviceId, instanceId);
    if (serviceInstance != null) {
      if (SERVICE_NAME.equals(propertyName)) {
        return serviceInstance.getServiceId();
      }
      return serviceInstance.getMetadata().get(propertyName);
    } else {
      LOGGER.error("not instance found {}-{}, maybe attack", instanceId, serviceId);
      return "";
    }
  }

  @Override
  public String getServiceName(String serviceId) {
    return serviceId;
  }

  private ServiceInstance getServiceInstance(String serviceId, String instanceId) {
    try {
      String key = String.format("%s@%s", serviceId, instanceId);
      return INSTANCES_CACHE.get(key, () -> {
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
}
