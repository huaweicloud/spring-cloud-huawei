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

package com.huaweicloud.servicecomb.discovery.registry;

import com.huaweicloud.common.cache.RegisterCache;
import org.junit.Test;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

import mockit.Injectable;

/**
 * @Author wangqijun
 * @Date 11:16 2019-08-16
 **/
public class HeartbeatSchedulerTest {

  @Injectable
  ServiceCombClient serviceCombClient;

  @Injectable
  ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  @Injectable
  TagsProperties tagsProperties;

  @Test
  public void addAndRemove() {
    serviceCombDiscoveryProperties.setHealthCheckInterval(10);
    HeartbeatScheduler heartbeatScheduler = new HeartbeatScheduler(serviceCombDiscoveryProperties,
        serviceCombClient, tagsProperties);
    RegisterCache.setInstanceID("11");
    heartbeatScheduler.add(null, null);
    heartbeatScheduler.remove();
  }
}