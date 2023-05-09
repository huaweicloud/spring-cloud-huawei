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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;

import com.huaweicloud.governance.authentication.MicroserviceInstanceService;
import com.huaweicloud.governance.authentication.instance.CommonInstance;

public class NacosRouterDistributor extends AbstractRouterDistributor<ServiceInstance, CommonInstance> {

  @Autowired
  public NacosRouterDistributor(MicroserviceInstanceService instance) {
    init(server -> instance.getMicroserviceInstance(server),
        CommonInstance::getVersion,
        CommonInstance::getServiceName,
        CommonInstance::getProperties);
  }
}
