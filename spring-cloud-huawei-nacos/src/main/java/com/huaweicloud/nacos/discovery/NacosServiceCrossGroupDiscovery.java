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
package com.huaweicloud.nacos.discovery;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.NacosServiceManager;
import com.alibaba.cloud.nacos.discovery.NacosServiceDiscovery;
import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosServiceCrossGroupDiscovery extends NacosServiceDiscovery {
  private final NacosServiceManager nacosServiceManager;

  private final NacosCrossGroupServiceConfig nacosCrossGroupServiceConfig;

  public NacosServiceCrossGroupDiscovery(NacosDiscoveryProperties discoveryProperties,
      NacosServiceManager nacosServiceManager, NacosCrossGroupServiceConfig nacosCrossGroupServiceConfig) {
    super(discoveryProperties, nacosServiceManager);
    this.nacosServiceManager = nacosServiceManager;
    this.nacosCrossGroupServiceConfig = nacosCrossGroupServiceConfig;
  }

  @Override
  public List<ServiceInstance> getInstances(String serviceId) throws NacosException {
    if (nacosCrossGroupServiceConfig.getServiceGroupMappings().containsKey(serviceId)) {
      String group = nacosCrossGroupServiceConfig.getServiceGroupMappings().get(serviceId);
      List<Instance> instances = nacosServiceManager.getNamingService().selectInstances(serviceId, group, true);
      return hostToServiceInstanceList(instances, serviceId);
    }
    return super.getInstances(serviceId);
  }

}
