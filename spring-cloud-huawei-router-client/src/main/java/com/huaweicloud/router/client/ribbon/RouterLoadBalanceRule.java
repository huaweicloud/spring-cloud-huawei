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
package com.huaweicloud.router.client.ribbon;

import java.util.List;

import com.huaweicloud.router.client.track.RouterTrackContext;
import com.huaweicloud.router.core.RouterFilter;

import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

/**
 * @Author GuoYl123
 * @Date 2019/10/11
 **/
public class RouterLoadBalanceRule extends ZoneAvoidanceRule {

  RouterDistributor distributor = new RouterDistributor();

  @Override
  public Server choose(Object key) {
    List<Server> serverList = RouterFilter
        .getFilteredListOfServers(getLoadBalancer().getReachableServers(),
            RouterTrackContext.getServiceName(),
            RouterTrackContext.getRequestHeader(),
            distributor);
    return super.getPredicate().chooseRoundRobinAfterFiltering(serverList, key).orNull();
  }
}
