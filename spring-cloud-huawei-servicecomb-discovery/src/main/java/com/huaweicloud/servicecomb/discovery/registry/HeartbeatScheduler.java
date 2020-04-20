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
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import com.huaweicloud.servicecomb.discovery.client.model.HeardBeatStatus;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;

import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.HeartbeatRequest;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import java.util.concurrent.atomic.AtomicBoolean;
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

  private TagsProperties tagsProperties;

  private ServiceCombSwaggerHandler serviceCombSwaggerHandler;

  private AtomicBoolean heartbeatLog = new AtomicBoolean(true);

  public HeartbeatScheduler(ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      ServiceCombClient serviceCombClient, TagsProperties tagsProperties) {
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
    this.serviceCombClient = serviceCombClient;
    this.tagsProperties = tagsProperties;
  }

  public void add(ServiceCombRegistration registration,
      ServiceCombSwaggerHandler serviceCombSwaggerHandler) {
    if (!serviceCombDiscoveryProperties.isHealthCheck()) {
      return;
    }
    this.serviceCombSwaggerHandler = serviceCombSwaggerHandler;
    ScheduledFuture currentTask = this.scheduler
        .scheduleWithFixedDelay(() -> {
              if (serviceCombSwaggerHandler != null) {
                serviceCombSwaggerHandler.initAndRegister();
              }
              try {
                HeartbeatRequest heartbeatRequest = new HeartbeatRequest(RegisterCache.getServiceID(),
                    RegisterCache.getInstanceID());
                HeardBeatStatus result = serviceCombClient.heartbeat(heartbeatRequest);
                if (result == HeardBeatStatus.FAILED) {
                  retryRegister(registration, heartbeatRequest.getInstances().get(0).getInstanceId());
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

  private void retryRegister(ServiceCombRegistration registration, String oldInstanceID) {
    LOGGER.info("retry registry to service center.");
    Microservice microservice = RegistryHandler.buildMicroservice(registration);
    try {
      String serviceID = serviceCombClient.getServiceId(microservice);
      if (null == serviceID) {
        serviceID = serviceCombClient.registerMicroservice(microservice);
      }
      if (serviceCombSwaggerHandler != null) {
        serviceCombSwaggerHandler.initAndRegister();
      }
      MicroserviceInstance microserviceInstance = RegistryHandler
          .buildMicroServiceInstances(serviceID, microservice, serviceCombDiscoveryProperties,
              tagsProperties);
      String instanceID = serviceCombClient.registerInstance(microserviceInstance);
      if (null != instanceID) {
        serviceCombClient.autoDiscovery(serviceCombDiscoveryProperties.isAutoDiscovery());
        RegisterCache.setInstanceID(instanceID);
        RegisterCache.setServiceID(serviceID);
        refreshLocalMap(instanceID, heartbeatRequestMap.remove(oldInstanceID));
        LOGGER.info("register success,instanceID:{};serviceID:{}", instanceID, serviceID);
      }
    } catch (ServiceCombException e) {
      serviceCombClient.toggle();
      LOGGER.warn(
          "register failed, will retry. please check config file. message=" + e.getMessage());
    }
  }
}
