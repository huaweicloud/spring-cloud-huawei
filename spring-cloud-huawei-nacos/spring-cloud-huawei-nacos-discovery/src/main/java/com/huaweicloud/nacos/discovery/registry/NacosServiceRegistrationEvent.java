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

import com.alibaba.nacos.api.naming.pojo.Instance;

public class NacosServiceRegistrationEvent {
  private final Instance instance;

  private final boolean success;

  public NacosServiceRegistrationEvent(Instance instance, boolean success) {
    this.instance = instance;
    this.success = success;
  }

  public Instance getInstance() {
    return instance;
  }

  public boolean isSuccess() {
    return success;
  }
}
