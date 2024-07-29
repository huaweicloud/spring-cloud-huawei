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

package com.huaweicloud.nacos.discovery.registry;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.core.Ordered;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.eventbus.EventBus;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.nacos.discovery.NacosConst;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.manager.NamingServiceManager;

public class NacosServiceRegistry implements ServiceRegistry<Registration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosServiceRegistry.class);

  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  private final List<NamingServiceManager> namingServiceManagers;

  private final Instance instance;

  private final EventBus eventBus;

  public NacosServiceRegistry(List<NamingServiceManager> namingServiceManagers,
      NacosDiscoveryProperties nacosDiscoveryProperties, Environment environment) {
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    this.namingServiceManagers = namingServiceManagers.stream().sorted(Comparator.comparingInt(Ordered::getOrder))
        .collect(Collectors.toList());
    this.instance =
        NacosMicroserviceHandler.createMicroserviceInstance(nacosDiscoveryProperties, environment);
    eventBus = EventManager.getEventBus();
  }

  @Override
  public void register(Registration registration) {
    if (StringUtils.isEmpty(registration.getServiceId())) {
      LOGGER.warn("Have no service name to register for nacos.");
      return;
    }
    String serviceId = registration.getServiceId();
    String group = nacosDiscoveryProperties.getGroup();
    instance.setInstanceId(registration.getInstanceId());
    int successCount = 0;
    for (NamingServiceManager serviceManager : namingServiceManagers) {
      try {
        serviceManager.getNamingService().registerInstance(serviceId, group, instance);
        updateServiceMetadata(serviceManager.getNamingMaintainService(), serviceId, group);
        successCount++;
        LOGGER.info("nacos registry, {} {}:{} register finished", serviceId, instance.getIp(), instance.getPort());
      } catch (Exception e) {
        LOGGER.error("service [{}] nacos server [{}] registry failed", serviceId, serviceManager.getServerAddr(), e);
      }
    }

    // just need one nacos server registry success
    if (successCount > 0) {
      eventBus.post(new NacosServiceRegistrationEvent(instance, true));
    } else {
      eventBus.post(new NacosServiceRegistrationEvent(instance, false));
    }
  }

  private void updateServiceMetadata(NamingMaintainService maintainService, String serviceId, String group) {
    Map<String, String> serviceMetadata = NacosMicroserviceHandler.createMicroserviceMetadata();
    if (!CollectionUtils.isEmpty(serviceMetadata)) {
      tryUpdateService(maintainService, serviceId, group, serviceMetadata);
    }
  }

  private void tryUpdateService(NamingMaintainService maintainService, String serviceId, String group,
      Map<String, String> serviceMetadata) {
    for (int i = 1; i < 4; i++) {
      try {
        // because of nacos register instance using async type, using delay mode to prevent service info update failed
        Thread.sleep(i * 1000);
        maintainService.updateService(serviceId, group, 0, serviceMetadata);
        break;
      } catch (Exception e) {
        LOGGER.warn("update service metadata failed, serviceName: {}, message: {}", serviceId, e.getMessage());
      }
    }
  }

  @Override
  public void deregister(Registration registration) {
    if (StringUtils.isEmpty(registration.getServiceId())) {
      LOGGER.warn("No service to de-register for nacos.");
      return;
    }
    int successCount = 0;
    for (NamingServiceManager serviceManager : namingServiceManagers) {
      String serviceId = registration.getServiceId();
      String group = nacosDiscoveryProperties.getGroup();
      try {
        serviceManager.getNamingService().deregisterInstance(serviceId, group, registration.getHost(),
            registration.getPort(), nacosDiscoveryProperties.getClusterName());
        successCount++;
      } catch (Exception e) {
        LOGGER.error("de-register service [{}] from Nacos Server [{}] failed.", registration.getServiceId(),
            serviceManager.getServerAddr(), e);
      }
    }

    // need all nacos server deregister success
    if (successCount == namingServiceManagers.size()) {
      eventBus.post(new NacosServiceRegistrationEvent(instance, false));
    }
  }

  @Override
  public void close() {
    for (NamingServiceManager serviceManager : namingServiceManagers) {
      try {
        serviceManager.shutDown();
      } catch (NacosException e) {
        LOGGER.error("Nacos server [{}] namingService shutDown failed.", serviceManager.getServerAddr(), e);
      }
    }
  }

  @Override
  public void setStatus(Registration registration, String status) {
    if (!NacosConst.STATUS_UP.equalsIgnoreCase(status) && !NacosConst.STATUS_DOWN.equalsIgnoreCase(status)) {
      LOGGER.warn("can't support status {} to update.", status);
      return;
    }
    String serviceId = registration.getServiceId();
    instance.setEnabled(!NacosConst.STATUS_DOWN.equalsIgnoreCase(status));
    for (int i = 0; i < namingServiceManagers.size(); i++) {
      try {
        namingServiceManagers.get(i).getNamingMaintainService()
            .updateInstance(serviceId, nacosDiscoveryProperties.getGroup(), instance);
      } catch (Exception e) {
        LOGGER.error("Nacos server [{}] service [{}] set status [{}] failed.",
            namingServiceManagers.get(i).getServerAddr(), serviceId, status, e);
        if (i == namingServiceManagers.size() - 1) {
          throw new RuntimeException("update nacos instance status fail", e);
        }
      }
    }
  }

  @Override
  @SuppressWarnings({"unchecked"})
  public <T> T getStatus(Registration registration) {
    String serviceName = registration.getServiceId();
    String group = nacosDiscoveryProperties.getGroup();
    for (NamingServiceManager serviceManager : namingServiceManagers) {
      try {
        List<Instance> instances = serviceManager.getNamingService().getAllInstances(serviceName, group);
        List<Instance> currentInstances = instances.stream().filter(this::checkIpAndPort).collect(Collectors.toList());
        if (!currentInstances.isEmpty()) {
          return (T) (currentInstances.get(0).isEnabled() ? NacosConst.STATUS_UP : NacosConst.STATUS_DOWN);
        }
      } catch (Exception e) {
        LOGGER.error("getStatus failed", e);
      }
    }
    return null;
  }

  private boolean checkIpAndPort(Instance instance) {
    return instance.getIp().equalsIgnoreCase(nacosDiscoveryProperties.getIp())
        && instance.getPort() == nacosDiscoveryProperties.getPort();
  }
}
