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
package com.huaweicloud.router.client;

import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;

import com.huaweicloud.common.instance.Instance;

public class SpringCloudRouterDistributor extends
    AbstractRouterDistributor<ServiceInstance, MicroserviceInstance> {
  private final Instance instance;

  @Autowired
  public SpringCloudRouterDistributor(Instance instance) {
    this.instance = instance;
    init(server -> createMicroserviceInstance(server),
        MicroserviceInstance::getVersion,
        MicroserviceInstance::getServiceName,
        MicroserviceInstance::getProperties);
  }

  private MicroserviceInstance createMicroserviceInstance(ServiceInstance microserviceInstanceService) {
    MicroserviceInstance microserviceInstance = instance.getMicroserviceInstance();
    if (microserviceInstance == null) {
      microserviceInstance = instance.getMicroserviceInstance(microserviceInstanceService);
    }
    return microserviceInstance;
  }
}