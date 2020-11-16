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
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.model.HeardBeatStatus;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * @Author wangqijun
 * @Date 16:33 2019-08-06
 **/
public class HeartbeatScheduler {

  private static final Logger LOGGER = LoggerFactory.getLogger(HeartbeatScheduler.class);

  private final TaskScheduler scheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());

  private final Map<String, ScheduledFuture> heartbeatRequestMap = new ConcurrentHashMap<>();

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private ServiceCombClient serviceCombClient;

  private AtomicBoolean heartbeatLog = new AtomicBoolean(true);

  public HeartbeatScheduler(ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      ServiceCombClient serviceCombClient) {
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
    this.serviceCombClient = serviceCombClient;
  }

  public void add(Microservice microservice, Function<Microservice, Boolean> registryFunc) {
    ScheduledFuture currentTask = this.scheduler
        .scheduleWithFixedDelay(() -> {
              try {
                HeardBeatStatus result = serviceCombClient
                    .heartbeat(RegisterCache.getServiceID(), RegisterCache.getInstanceID());
                if (result == HeardBeatStatus.FAILED) {
                  LOGGER.info("retry registry to service center.");
                  String oldInstanceID = RegisterCache.getInstanceID();
                  if (registryFunc.apply(microservice)) {
                    refreshLocalMap(RegisterCache.getInstanceID(), heartbeatRequestMap.remove(oldInstanceID));
                  }
                }
                if (heartbeatLog.get()) {
                  LOGGER.info("heartbeat success.");
                  heartbeatLog.compareAndSet(true, false);
                }
              } catch (ServiceCombException e) {
                heartbeatLog.compareAndSet(false, true);
                LOGGER.warn("heartbeat failed.", e);
              }
            },
            serviceCombDiscoveryProperties.getHealthCheckInterval() * 1000);
    refreshLocalMap(RegisterCache.getInstanceID(), currentTask);
  }

  public void remove() {
    ScheduledFuture scheduled = heartbeatRequestMap.remove(RegisterCache.getInstanceID());
    if (null != scheduled) {
      scheduled.cancel(true);
    }
  }

  private void refreshLocalMap(String instanceId, ScheduledFuture currentTask) {
    ScheduledFuture preScheduled = heartbeatRequestMap.put(instanceId, currentTask);
    if (null != preScheduled) {
      preScheduled.cancel(true);
    }
  }
}
