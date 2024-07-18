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

package com.huaweicloud.servicecomb.discovery.ribbon;

import java.util.List;

import com.huaweicloud.common.context.InvocationContext;
import com.netflix.loadbalancer.AbstractServerList;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;

public class ServiceCombLoadBalancer implements ILoadBalancer {
  private final IRule rule;

  private final AbstractServerList<Server> serverList;

  public ServiceCombLoadBalancer(IRule rule,
      AbstractServerList<Server> serverList) {
    this.rule = rule;
    this.rule.setLoadBalancer(this);
    this.serverList = serverList;
  }

  @Override
  public void addServers(List<Server> newServers) {
    // do nothing
  }

  @Override
  public Server chooseServer(Object key) {
    Server server = this.rule.choose(key);
    InvocationContext.setCurrentInstanse(server);
    return server;
  }

  @Override
  public void markServerDown(Server server) {
    // do nothing
  }

  @Override
  @SuppressWarnings("deprecation")
  public List<Server> getServerList(boolean availableOnly) {
    return serverList.getUpdatedListOfServers();
  }

  @Override
  public List<Server> getReachableServers() {
    return serverList.getUpdatedListOfServers();
  }

  @Override
  public List<Server> getAllServers() {
    return serverList.getUpdatedListOfServers();
  }
}
