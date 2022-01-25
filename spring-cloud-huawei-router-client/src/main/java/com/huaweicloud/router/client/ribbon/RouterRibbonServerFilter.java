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
package com.huaweicloud.router.client.ribbon;

import java.util.List;

import org.apache.servicecomb.router.RouterFilter;
import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.beans.factory.annotation.Autowired;

import com.huaweicloud.common.ribbon.RibbonServerFilter;
import com.huaweicloud.router.client.track.RouterTrackContext;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServer;
import com.netflix.loadbalancer.Server;

public class RouterRibbonServerFilter implements RibbonServerFilter {
  @Autowired
  private AbstractRouterDistributor<Server, MicroserviceInstance> routerDistributor;

  @Autowired
  private RouterFilter routerFilter;

  @Override
  public List<Server> filter(List<Server> instances) {
    if (instances.isEmpty()) {
      return instances;
    }

    return routerFilter
        .getFilteredListOfServers(instances,
            ((ServiceCombServer) instances.get(0)).getServiceCombServiceInstance().getServiceId(),
            RouterTrackContext.getRequestHeader(),
            routerDistributor);
  }

  @Override
  public int getOrder() {
    return 1;
  }
}