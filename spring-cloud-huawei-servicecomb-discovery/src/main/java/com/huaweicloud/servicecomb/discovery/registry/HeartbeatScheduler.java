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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.HeartbeatRequest;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * @Author wangqijun
 * @Date 16:33 2019-08-06
 **/
public class HeartbeatScheduler {

  private final TaskScheduler scheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());

  private final Map<String, ScheduledFuture> heartbeatRequestMap = new ConcurrentHashMap<>();

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private ServiceCombClient serviceCombClient;

  public HeartbeatScheduler(ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      ServiceCombClient serviceCombClient) {
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
    this.serviceCombClient = serviceCombClient;
  }

  public void add(String instanceId, String serviceId) {
    if (!serviceCombDiscoveryProperties.isHealthCheck()) {
      return;
    }
    HeartbeatRequest heartbeatRequest = new HeartbeatRequest(serviceId, instanceId);
    ScheduledFuture currentTask = this.scheduler
        .scheduleWithFixedDelay(new HeartbeatTask(heartbeatRequest, serviceCombClient),
            serviceCombDiscoveryProperties.getHealthCheckInterval() * 1000);
    ScheduledFuture preScheduled = heartbeatRequestMap.put(instanceId, currentTask);
    if (null != preScheduled) {
      preScheduled.cancel(true);
    }
  }

  public void remove(String instanceId) {
    ScheduledFuture scheduled = heartbeatRequestMap.get(instanceId);
    if (null != scheduled) {
      scheduled.cancel(true);
    }
    heartbeatRequestMap.remove(instanceId);
  }
}
