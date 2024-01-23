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
package com.huaweicloud.servicecomb.discovery.authentication;

import java.beans.PropertyDescriptor;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.UncheckedExecutionException;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.Const;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

public class ServiceCombAuthenticationAdapter implements AuthenticationAdapter {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombAuthenticationAdapter.class);

  private static final Cache<String, Microservice> MICROSERVICE_CACHE = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  private static final Cache<String, MicroserviceInstance> INSTANCES_CACHE = CacheBuilder.newBuilder()
      .maximumSize(1000)
      .expireAfterAccess(30, TimeUnit.MINUTES)
      .build();

  private final ServiceCenterClient serviceCenterClient;

  public ServiceCombAuthenticationAdapter(ServiceCenterClient serviceCenterClient) {
    this.serviceCenterClient = serviceCenterClient;
  }

  @Override
  public String getInstanceId(Registration registration) {
    ServiceCombRegistration serviceCombRegistration = (ServiceCombRegistration) registration;
    return serviceCombRegistration.getMicroserviceInstance().getInstanceId();
  }

  @Override
  public String getServiceId(Registration registration) {
    ServiceCombRegistration serviceCombRegistration = (ServiceCombRegistration) registration;
    return serviceCombRegistration.getMicroserviceInstance().getServiceId();
  }

  @Override
  public void setPublicKey(Registration registration, String publicKey) {
    ServiceCombRegistration serviceCombRegistration = (ServiceCombRegistration) registration;
    serviceCombRegistration.getMicroserviceInstance().getProperties().put(Const.INSTANCE_PUBKEY_PRO, publicKey);
  }

  @Override
  public String getPublicKeyFromInstance(String instanceId, String serviceId) {
    MicroserviceInstance instances = getOrCreate(serviceId, instanceId);
    if (instances != null) {
      return instances.getProperties().get(Const.INSTANCE_PUBKEY_PRO);
    } else {
      LOGGER.error("not instance found {}-{}, maybe attack", instanceId, serviceId);
      return "";
    }
  }

  @Override
  public String getPropertyValue(String serviceId, String instanceId, String propertyName) {
    Microservice microservice = getOrCreate(serviceId);
    try {
      Object fieldValue = new PropertyDescriptor(propertyName, Microservice.class).getReadMethod().invoke(microservice);
      if (fieldValue.getClass().getName().equals(String.class.getName())) {
        return (String) fieldValue;
      }
    } catch (Exception e) {
      LOGGER.warn("can't find property name: {} in microservice field.", propertyName);
    }
    return microservice.getProperties().get(propertyName);
  }

  @Override
  public String getServiceName(String serviceId) {
    Microservice microservice = getOrCreate(serviceId);
    if (microservice != null) {
      return microservice.getServiceName();
    }
    return serviceId;
  }

  private Microservice getOrCreate(String serviceId) {
    try {
      return MICROSERVICE_CACHE.get(serviceId, () -> {
        Microservice microservice = serviceCenterClient.getMicroserviceByServiceId(serviceId);
        if (microservice == null) {
          throw new IllegalArgumentException("service id not exists.");
        }
        return microservice;
      });
    } catch (ExecutionException | UncheckedExecutionException e) {
      LOGGER.error("get microservice from cache failed, {}, {}", serviceId, e.getMessage());
      return null;
    }
  }

  private MicroserviceInstance getOrCreate(String serviceId, String instanceId) {
    try {
      String key = String.format("%s@%s", serviceId, instanceId);
      return INSTANCES_CACHE.get(key, () -> {
        MicroserviceInstance instance = serviceCenterClient.getMicroserviceInstance(serviceId, instanceId);
        if (instance == null) {
          throw new IllegalArgumentException("instance id not exists.");
        }
        return instance;
      });
    } catch (ExecutionException | UncheckedExecutionException e) {
      LOGGER.error("get microservice instance from cache failed, {}, {}",
          String.format("%s@%s", serviceId, instanceId),
          e.getMessage());
      return null;
    }
  }
}
