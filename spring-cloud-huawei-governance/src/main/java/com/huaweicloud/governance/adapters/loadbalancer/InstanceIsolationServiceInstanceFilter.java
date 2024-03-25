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
package com.huaweicloud.governance.adapters.loadbalancer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.disovery.InstanceIDAdapter;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.governance.event.InstanceIsolatedEvent;

public class InstanceIsolationServiceInstanceFilter implements ServiceInstanceFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(InstanceIsolationServiceInstanceFilter.class);

  private static final String INSTAANCE_PING_ENABLED = "spring.cloud.servicecomb.isolation.instance.ping.enabled";

  private final Object lock = new Object();

  private final Map<String, Long> isolatedInstances = new ConcurrentHashMap<>();

  private final Environment env;

  private final FallbackDiscoveryProperties fallbackDiscoveryProperties;

  public InstanceIsolationServiceInstanceFilter(Environment environment,
      FallbackDiscoveryProperties fallbackDiscoveryProperties) {
    this.env = environment;
    this.fallbackDiscoveryProperties = fallbackDiscoveryProperties;
    EventManager.register(this);
  }

  @Subscribe
  @SuppressWarnings("unused")
  public void onInstanceIsolatedEvent(InstanceIsolatedEvent event) {
    synchronized (lock) {
      for (Iterator<String> iterator = isolatedInstances.keySet().iterator(); iterator.hasNext(); ) {
        Long duration = isolatedInstances.get(iterator.next());
        if (System.currentTimeMillis() - duration > 0) {
          iterator.remove();
        }
      }

      isolatedInstances.put(event.getInstanceId(),
          System.currentTimeMillis() + event.getWaitDurationInHalfOpenState().toMillis());
    }
  }

  @Override
  public List<ServiceInstance> filter(ServiceInstanceListSupplier supplier, List<ServiceInstance> instances,
      Request<?> request) {
    if (instances.isEmpty()) {
      return fallbackServiceInstance(supplier, instances);
    }
    if (isolatedInstances.isEmpty()) {
      return instances;
    }
    List<ServiceInstance> result = new ArrayList<>(instances.size());
    for (ServiceInstance serviceInstance : instances) {
      Long duration = isolatedInstances.get(InstanceIDAdapter.instanceId(serviceInstance));
      if (duration == null) {
        result.add(serviceInstance);
        continue;
      }

      if (System.currentTimeMillis() - duration < 0) {
        continue;
      }
      if (checkInstanceHealth(serviceInstance)) {
        synchronized (lock) {
          isolatedInstances.remove(InstanceIDAdapter.instanceId(serviceInstance));
        }
      } else {
        continue;
      }
      result.add(serviceInstance);
    }

    if (result.isEmpty()) {
      return fallbackServiceInstance(supplier, instances);
    }
    return result;
  }

  private List<ServiceInstance> fallbackServiceInstance(ServiceInstanceListSupplier supplier,
      List<ServiceInstance> instances) {
    if (fallbackDiscoveryProperties.isEnabled()
        && fallbackDiscoveryProperties.readFallbackServiceInstance(supplier.getServiceId()) != null) {
      return List.of(fallbackDiscoveryProperties.readFallbackServiceInstance(supplier.getServiceId()));
    }
    return instances;
  }

  private boolean checkInstanceHealth(ServiceInstance instance) {
    if (!env.getProperty(INSTAANCE_PING_ENABLED, boolean.class, false)) {
      return true;
    }
    try (Socket s = new Socket()) {
      s.connect(new InetSocketAddress(instance.getHost(), instance.getPort()), 3000);
      return true;
    } catch (IOException e) {
      LOGGER.warn("ping instance {} failed, It will be quarantined again.", instance);
    }
    return false;
  }

  @Override
  public int getOrder() {
    return env.getProperty("spring.cloud.loadbalance.filter.instance-isolation.order", int.class, -300);
  }
}
