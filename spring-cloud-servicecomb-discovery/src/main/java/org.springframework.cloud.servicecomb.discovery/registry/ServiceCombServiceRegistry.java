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

package org.springframework.cloud.servicecomb.discovery.registry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.cloud.servicecomb.discovery.client.ServiceCombClient;
import org.springframework.cloud.servicecomb.discovery.client.exception.ServiceCombException;
import org.springframework.cloud.servicecomb.discovery.client.model.HealthCheck;
import org.springframework.cloud.servicecomb.discovery.client.model.HealthCheckMode;
import org.springframework.cloud.servicecomb.discovery.client.model.HeartbeatRequest;
import org.springframework.cloud.servicecomb.discovery.client.model.Microservice;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceInstance;
import org.springframework.cloud.servicecomb.discovery.client.model.MicroserviceStatus;
import org.springframework.cloud.servicecomb.discovery.client.util.NetUtil;
import org.springframework.cloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

public class ServiceCombServiceRegistry implements ServiceRegistry<ServiceCombRegistration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServiceRegistry.class);


  private ServiceCombClient serviceCombClient;

  public ServiceCombServiceRegistry(ServiceCombClient serviceCombClient) {
    this.serviceCombClient = serviceCombClient;
  }

  @Override
  public void register(ServiceCombRegistration registration) {
    String serviceID = null;
    String instanceID = null;
    Microservice microservice = buildMicroservice(registration);
    while (true) {
      try {
        serviceID = serviceCombClient.getMicroserviceID(microservice);
        MicroserviceInstance microserviceInstance = buildMicroServiceInstances(serviceID, microservice);
        if (null == serviceID) {
          serviceID = serviceCombClient.registerMicroservice(microservice);
          if (null != serviceID) {
            break;
          }
        } else {
          instanceID = serviceCombClient.registerInstance(microserviceInstance);
          if (!instanceID.isEmpty()) {
            break;
          }
        }
      } catch (ServiceCombException e) {
        LOGGER.warn("register failed, will retry. please check config file. message=" + e.getMessage());
      }
      try {
        Thread.sleep(10 * 1000);//TODO exact to config
      } catch (InterruptedException e) {
        LOGGER.warn("thread interrupted.");
      }
    }
    LOGGER.info("register success,instanceID=" + instanceID + ";serviceID=" + serviceID);
    heartbeat(serviceID, instanceID);
  }

  @Override
  public void deregister(ServiceCombRegistration registration) {

  }

  private MicroserviceInstance buildMicroServiceInstances(String serviceID, Microservice microservice) {
    MicroserviceInstance microserviceInstance = buildInstance(serviceID);
    List<MicroserviceInstance> instances = new ArrayList<>();
    instances.add(microserviceInstance);
    microservice.setInstances(instances);
    microservice.setStatus(MicroserviceStatus.UP);
    return microserviceInstance;
  }

  private void heartbeat(String serviceID, String instanceID) {
    TaskScheduler scheduler = new ConcurrentTaskScheduler(Executors.newSingleThreadScheduledExecutor());
    HeartbeatRequest heartbeatRequest = new HeartbeatRequest(serviceID, instanceID);
    scheduler.scheduleWithFixedDelay(new HeartbeatTask(heartbeatRequest, serviceCombClient), 10000);
  }

  private MicroserviceInstance buildInstance(String serviceID) {
    MicroserviceInstance microserviceInstance = new MicroserviceInstance();
    microserviceInstance.setServiceId(serviceID);
    microserviceInstance.setHostName(NetUtil.getLocalHost());//TODO
    List<String> endPoints = new ArrayList<>();
    endPoints.add("http://127.0.0.1:8080");//TODO
    microserviceInstance.setEndpoints(endPoints);
    HealthCheck healthCheck = new HealthCheck();
    healthCheck.setMode(HealthCheckMode.PLATFORM);
    healthCheck.setInterval(3000);
    healthCheck.setTimes(3);
    microserviceInstance.setHealthCheck(healthCheck);
    return microserviceInstance;
  }

  private Microservice buildMicroservice(ServiceCombRegistration registration) {
    Microservice microservice = new Microservice();
    microservice.setAppId(registration.getAppName());
    microservice.setServiceName(registration.getServiceId());
    microservice.setVersion(registration.getVersion());
    return microservice;
  }


  @Override
  public void close() {
    LOGGER.info("close");
  }

  @Override
  public void setStatus(ServiceCombRegistration registration, String status) {

  }

  @Override
  public <T> T getStatus(ServiceCombRegistration registration) {
    return null;
  }
}
