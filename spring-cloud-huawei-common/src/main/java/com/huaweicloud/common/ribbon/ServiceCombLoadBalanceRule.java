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
package com.huaweicloud.common.ribbon;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

/**
 * @Author GuoYl123
 * @Date 2019/10/11
 **/
public class ServiceCombLoadBalanceRule extends ZoneAvoidanceRule {

  @Autowired
  private List<RibbonServerFilter> list;

  @Override
  public Server choose(Object key) {
    List<Server> serverList = getLoadBalancer().getReachableServers();
    for (RibbonServerFilter filter : list) {
      serverList = filter.filter(serverList);
    }
    Server lastInvoke = super.getPredicate().chooseRoundRobinAfterFiltering(serverList, key).orNull();
    return lastInvoke;
  }
}
