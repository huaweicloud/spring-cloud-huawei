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

import org.apache.curator.x.discovery.ServiceInstance;

import com.huaweicloud.zookeeper.discovery.ZookeeperServiceInstance;

public class ZookeeperServiceRegistrationEvent {
  private final ServiceInstance<ZookeeperServiceInstance> serviceInstance;

  private final boolean success;

  public ZookeeperServiceRegistrationEvent(ServiceInstance<ZookeeperServiceInstance> instance, boolean success) {
    this.serviceInstance = instance;
    this.success = success;
  }

  public ServiceInstance<ZookeeperServiceInstance> getInstance() {
    return serviceInstance;
  }

  public boolean isSuccess() {
    return success;
  }
}
