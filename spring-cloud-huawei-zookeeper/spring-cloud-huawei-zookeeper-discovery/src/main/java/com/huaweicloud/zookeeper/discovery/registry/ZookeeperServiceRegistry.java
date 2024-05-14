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

package com.huaweicloud.zookeeper.discovery.registry;

import org.apache.commons.lang3.StringUtils;
import org.apache.curator.x.discovery.ServiceDiscovery;
import org.apache.curator.x.discovery.ServiceInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.EventBus;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.zookeeper.discovery.ZookeeperConstants;
import com.huaweicloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import com.huaweicloud.zookeeper.discovery.ZookeeperServiceInstance;

public class ZookeeperServiceRegistry implements ServiceRegistry<Registration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperServiceRegistry.class);

  private final ServiceInstance<ZookeeperServiceInstance> instance;

  private final ServiceDiscovery<ZookeeperServiceInstance> serviceDiscovery;

  private final EventBus eventBus;

  public ZookeeperServiceRegistry(ZookeeperDiscoveryProperties properties, Environment environment,
      ZookeeperRegistration registration, ServiceDiscovery<ZookeeperServiceInstance> serviceDiscovery) {
    this.instance =
        ZookeeperMicroserviceHandler.createMicroserviceInstance(properties, environment, registration);
    this.serviceDiscovery = serviceDiscovery;
    eventBus = EventManager.getEventBus();
  }

  @Override
  public void register(Registration registration) {
    if (StringUtils.isEmpty(registration.getServiceId())) {
      LOGGER.warn("Have no service name to register for zookeeper.");
      return;
    }
    try {
      this.serviceDiscovery.registerService(instance);
      eventBus.post(new ZookeeperServiceRegistrationEvent(instance, true));
    } catch (Exception e) {
      LOGGER.error("service {} zookeeper registry failed", registration.getServiceId(), e);
    }
  }

  @Override
  public void deregister(Registration registration) {
    LOGGER.warn("De-registery service {} from Zookeeper Server started.", registration.getServiceId());
    if (StringUtils.isEmpty(registration.getServiceId())) {
      LOGGER.warn("No service to de-register for zookeeper.");
      return;
    }
    try {
      this.serviceDiscovery.unregisterService(instance);
      eventBus.post(new ZookeeperServiceRegistrationEvent(instance, false));
    } catch (Exception e) {
      LOGGER.error("de-register service {} from zookeeper Server failed.", registration.getServiceId(), e);
    }
    LOGGER.info("De-registery service {} from Zookeeper Server finished.", registration.getServiceId());
  }

  @Override
  public void close() {
    try {
      this.serviceDiscovery.close();
    } catch (Exception e) {
      LOGGER.error("Zookeeper serviceDiscovery shutDown failed.", e);
    }
  }

  @Override
  public void setStatus(Registration registration, String status) {
    if (!ZookeeperConstants.STATUS_UP.equalsIgnoreCase(status)
        && !ZookeeperConstants.STATUS_DOWN.equalsIgnoreCase(status)) {
      LOGGER.warn("can't support status {} to update.", status);
      return;
    }
    instance.getPayload().setStatus(status);
    try {
      this.serviceDiscovery.updateService(instance);
    } catch (Exception e) {
      throw new RuntimeException("update zookeeper instance status fail", e);
    }
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public <T> T getStatus(Registration registration) {
    return (T) instance.getPayload().getStatus();
  }
}
