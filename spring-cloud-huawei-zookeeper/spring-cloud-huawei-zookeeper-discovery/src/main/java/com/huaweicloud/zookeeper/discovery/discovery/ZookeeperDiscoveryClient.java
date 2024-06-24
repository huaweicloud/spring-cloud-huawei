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

package com.huaweicloud.zookeeper.discovery.discovery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.curator.x.discovery.ServiceDiscovery;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

import com.huaweicloud.zookeeper.discovery.ZookeeperConstants;
import com.huaweicloud.zookeeper.discovery.ZookeeperDiscoveryProperties;
import com.huaweicloud.zookeeper.discovery.ZookeeperServiceInstance;

public class ZookeeperDiscoveryClient implements DiscoveryClient {
  public static final String DESCRIPTION = "Zookeeper Discovery Client";

  private final ServiceDiscovery<ZookeeperServiceInstance> serviceDiscovery;

  private final ZookeeperDiscoveryProperties discoveryProperties;

  public ZookeeperDiscoveryClient(ServiceDiscovery<ZookeeperServiceInstance> serviceDiscovery,
      ZookeeperDiscoveryProperties discoveryProperties) {
    this.discoveryProperties = discoveryProperties;
    this.serviceDiscovery = serviceDiscovery;
  }

  @Override
  public String description() {
    return DESCRIPTION;
  }

  @Override
  public List<ServiceInstance> getInstances(String serviceId) {
    List<ServiceInstance> instances = new ArrayList<>();
    String queryServiceId = serviceId;
    if (discoveryProperties.getDiscoveryPathForAlias().containsKey(serviceId)) {
      queryServiceId = discoveryProperties.getDiscoveryPathForAlias().get(serviceId);
    }
    try {
      Collection<org.apache.curator.x.discovery.ServiceInstance<ZookeeperServiceInstance>> zkInstances
          = this.serviceDiscovery.queryForInstances(queryServiceId);
      for (org.apache.curator.x.discovery.ServiceInstance<ZookeeperServiceInstance> instance : zkInstances) {
        if (ZookeeperConstants.STATUS_UP.equalsIgnoreCase(instance.getPayload().getStatus())) {
          instances.add(instance.getPayload());
        }
      }
      return instances;
    } catch (Exception e) {
      throw new RuntimeException("Can not get setvice from Zookeeper server. serviceId: " + queryServiceId, e);
    }
  }

  @Override
  public List<String> getServices() {
    try {
      Collection<String> names = this.serviceDiscovery.queryForNames();
      if (names == null) {
        return Collections.emptyList();
      }
      return new ArrayList<>(names);
    } catch (Exception e) {
      throw new RuntimeException("get service name from Zookeeper server failed.", e);
    }
  }
}
