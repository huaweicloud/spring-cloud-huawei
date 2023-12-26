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
package com.huaweicloud.governance.adapters.loadbalancer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.core.env.Environment;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.disovery.InstanceIDAdapter;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.governance.event.InstanceIsolatedEvent;

public class InstanceIsolationServiceInstanceFilter implements ServiceInstanceFilter {
  private final Object lock = new Object();

  private final Map<String, Long> isolatedInstances = new ConcurrentHashMap<>();

  private final Environment env;

  public InstanceIsolationServiceInstanceFilter(Environment environment) {
    this.env = environment;
    EventManager.register(this);
  }

  @Subscribe
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
    if (isolatedInstances.isEmpty() || instances.isEmpty()) {
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

      synchronized (lock) {
        isolatedInstances.remove(InstanceIDAdapter.instanceId(serviceInstance));
      }
      result.add(serviceInstance);
    }

    if (result.isEmpty()) {
      return instances;
    }
    return result;
  }

  @Override
  public int getOrder() {
    return env.getProperty("spring.cloud.loadbalance.filter.instance-isolation.order", int.class, -300);
  }
}
