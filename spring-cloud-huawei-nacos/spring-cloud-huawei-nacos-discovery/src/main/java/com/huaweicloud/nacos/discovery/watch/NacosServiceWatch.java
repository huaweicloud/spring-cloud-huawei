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

package com.huaweicloud.nacos.discovery.watch;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.listener.EventListener;
import com.alibaba.nacos.api.naming.listener.NamingEvent;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.event.ClosedEventListener;
import com.huaweicloud.common.event.ClosedEventProcessor;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.NamingServiceManager;
import com.huaweicloud.nacos.discovery.registry.NacosServiceRegistrationEvent;

public class NacosServiceWatch {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosServiceWatch.class);

  private final NacosDiscoveryProperties properties;

  private final NamingServiceManager serviceManager;

  private EventListener eventListener;

  public NacosServiceWatch(NacosDiscoveryProperties discoveryProperties,
      NamingServiceManager namingServiceManager, ClosedEventListener closedEventListener) {
    this.serviceManager = namingServiceManager;
    this.properties = discoveryProperties;
    EventManager.register(this);
    closedEventListener.addClosedEventProcessor(new ClosedEventProcessor() {
      @Override
      public void process() {
        stopSubscribe();
      }

      @Override
      public int getOrder() {
        return 10;
      }
    });
  }

  @Subscribe
  public void subscribeServiceMetadata(NacosServiceRegistrationEvent registrationEvent) {
    if (registrationEvent.isSuccess()) {
      eventListener = event -> {
        if (event instanceof NamingEvent) {
          List<Instance> instances = ((NamingEvent) event).getInstances();
          Optional<Instance> instanceOptional = selectServiceSelfInstance(instances);
          instanceOptional.ifPresent(this::resetMetadataIfChanged);
        }
      };
      try {
        NamingService namingService = serviceManager.buildNamingService();
        namingService.subscribe(properties.getService(), properties.getGroup(),
            Arrays.asList(properties.getClusterName()), eventListener);
      } catch (Exception e) {
        LOGGER.error("subscribe service={}, group={} metadata failed!", properties.getService(),
            properties.getGroup(), e);
      }
    }
  }

  private void resetMetadataIfChanged(Instance instance) {
    if (!properties.getMetadata().equals(instance.getMetadata())) {
      properties.setMetadata(instance.getMetadata());
    }
  }

  private Optional<Instance> selectServiceSelfInstance(List<Instance> instances) {
    return instances.stream()
        .filter(instance -> properties.getIp().equals(instance.getIp()) && properties.getPort() == instance.getPort())
        .findFirst();
  }

  private void stopSubscribe() {
    try {
      NamingService namingService = serviceManager.buildNamingService();
      namingService.unsubscribe(properties.getService(), properties.getGroup(),
          Arrays.asList(properties.getClusterName()), eventListener);
      LOGGER.error("stopSubscribe service={}, group={} metadata finished!", properties.getService(),
          properties.getGroup());
    } catch (Exception e) {
      LOGGER.error("stopSubscribe service={}, group={} metadata failed!", properties.getService(),
          properties.getGroup(), e);
    }
  }
}
