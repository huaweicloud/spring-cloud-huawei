/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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
import org.springframework.cloud.client.ServiceInstance;

import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;

public class SpringCloudRouterDistributor extends
    AbstractRouterDistributor<ServiceInstance, MicroserviceInstance> {
  public SpringCloudRouterDistributor() {
    init(server -> ((ServiceCombServiceInstance) server).getMicroserviceInstance(),
        MicroserviceInstance::getVersion,
        MicroserviceInstance::getServiceName,
        MicroserviceInstance::getProperties);
  }
}
