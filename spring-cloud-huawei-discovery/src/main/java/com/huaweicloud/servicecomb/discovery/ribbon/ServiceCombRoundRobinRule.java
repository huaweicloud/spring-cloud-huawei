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

import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.Server;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class ServiceCombRoundRobinRule implements IRule {
  private final AtomicInteger counter = new AtomicInteger(0);

  private ILoadBalancer loadBalancer;

  @Override
  public Server choose(Object key) {
    List<Server> servers = loadBalancer.getReachableServers();
    if (CollectionUtils.isEmpty(servers)) {
      return null;
    }
    int index = Math.abs(counter.getAndIncrement()) % servers.size();
    return servers.get(index);
  }

  @Override
  public void setLoadBalancer(ILoadBalancer lb) {
    this.loadBalancer = lb;
  }

  @Override
  public ILoadBalancer getLoadBalancer() {
    return this.loadBalancer;
  }
}
