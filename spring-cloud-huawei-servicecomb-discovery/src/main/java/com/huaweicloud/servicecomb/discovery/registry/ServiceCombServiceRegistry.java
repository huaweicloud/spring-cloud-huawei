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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import com.huaweicloud.common.log.logConstantValue;
import com.huaweicloud.servicecomb.discovery.log.GenerateLog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

import com.huaweicloud.common.cache.RegisterCache;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstance;
import com.huaweicloud.servicecomb.discovery.client.model.MicroserviceInstanceSingleResponse;
import com.huaweicloud.servicecomb.discovery.client.model.SchemaResponse;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

public class ServiceCombServiceRegistry implements ServiceRegistry<ServiceCombRegistration> {

  @Autowired(required = false)
  private ServiceCombSwaggerHandler serviceCombSwaggerHandler;

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServiceRegistry.class);

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private ServiceCombClient serviceCombClient;

  private HeartbeatScheduler heartbeatScheduler;

  private TagsProperties tagsProperties;

  private String serviceID = null;

  private String instanceID = null;

  private ServiceCombWatcher serviceCombWatcher;

  private ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, (r) -> {
    Thread thread = new Thread(r);
    thread.setName("com.huaweicloud.registry");
    thread.setDaemon(true);
    return thread;
  });

  public ServiceCombServiceRegistry(ServiceCombClient serviceCombClient,
      HeartbeatScheduler heartbeatScheduler,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties,
      ServiceCombWatcher serviceCombWatcher,
      TagsProperties tagsProperties) {
    this.serviceCombWatcher = serviceCombWatcher;
    this.tagsProperties = tagsProperties;
    this.serviceCombClient = serviceCombClient;
    this.heartbeatScheduler = heartbeatScheduler;
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
  }

  @Override
  public void register(ServiceCombRegistration registration) {
    asyncRegister(registration);
  }

  private void asyncRegister(ServiceCombRegistration registration) {
    EXECUTOR.execute(() -> {
      try {
        loopRegister(registration);
        RegisterCache.setInstanceID(instanceID);
        RegisterCache.setServiceID(serviceID);
        if (serviceCombDiscoveryProperties.isWatch()) {
          startWatch();
        }
        LOGGER.info("register success,instanceID=" + instanceID + ";serviceID=" + serviceID);
        LOGGER.info(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_INFO,
            "register success.", logConstantValue.EVENT_CREATE));
        heartbeatScheduler.add(registration);
      } catch (Throwable e) {
        LOGGER.error("Unexpected exception in register. ", e);
        LOGGER.error(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_ERROR,
            "register failed.", logConstantValue.EVENT_CREATE));
      }
    });
  }

  private void startWatch() {
    try {
      URI uri = new URI(serviceCombClient.getUrl());
      String url = uri.getHost() + (uri.getPort() == -1 ? "" : (":" + uri.getPort()))
          + "/v4/" + ServiceRegistryConfig.DEFAULT_PROJECT
          + "/registry/microservices/" + serviceID + "/watcher";
      serviceCombWatcher.start(url);
    } catch (URISyntaxException e) {
      LOGGER.error("parse url error");
      LOGGER.error(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_ERROR,
          "parse url error.", logConstantValue.EVENT_WATCH));
    }
  }

  private void loopRegister(ServiceCombRegistration registration) {
    Microservice microservice = RegistryHandler.buildMicroservice(registration);
    if (serviceCombSwaggerHandler != null) {
      serviceCombSwaggerHandler.init(serviceCombDiscoveryProperties.getAppName(),
          serviceCombDiscoveryProperties.getServiceName());
      microservice.setSchemas(serviceCombSwaggerHandler.getSchemaIds());
    }
    while (true) {
      try {
        serviceID = serviceCombClient.getServiceId(microservice);
        if (null == serviceID) {
          serviceID = serviceCombClient.registerMicroservice(microservice);
          if (serviceCombSwaggerHandler != null) {
            serviceCombSwaggerHandler.registerSwagger(serviceID, microservice.getSchemas());
          }
        } else if (serviceCombSwaggerHandler != null) {
          List<String> schemas = filterSchema(serviceCombSwaggerHandler.getSchemasSummaryMap());
          serviceCombSwaggerHandler.registerSwagger(serviceID, schemas);
        }
        MicroserviceInstance microserviceInstance = RegistryHandler
            .buildMicroServiceInstances(serviceID, microservice, serviceCombDiscoveryProperties,
                tagsProperties);
        instanceID = serviceCombClient.registerInstance(microserviceInstance);
        if (null != instanceID) {
          serviceCombClient.autoDiscovery(serviceCombDiscoveryProperties.isAutoDiscovery());
          break;
        }
      } catch (ServiceCombException e) {
        serviceCombClient.toggle();
        LOGGER.warn(
            "register failed, will retry. please check config file. message=" + e.getMessage());
        LOGGER.warn(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_WARN,
            "register failed.", logConstantValue.EVENT_POLL));
      }
    }
  }

  /**
   * production时：
   * 1.先检查，对于已经注册的契约：i.没有删除 ii.没有summary变更的schema
   * 2.检查不通过 启动失败
   * 3.检查成功，只注册新增的接口
   * 非production时:
   * 1.全部重新注册
   *
   * @param localSchemas
   * @return
   * @throws ServiceCombException
   */
  private List<String> filterSchema(Map<String, String> localSchemas) throws ServiceCombException {

    if (!serviceCombDiscoveryProperties.getEnvironment().equals("production")) {
      return new ArrayList<>(localSchemas.keySet());
    }
    SchemaResponse schemas = serviceCombClient.getSchemas(serviceID);
    schemas.getSchemas().forEach(schema -> {
      if (!localSchemas.containsKey(schema.getSchemaId()) ||
          !localSchemas.get(schema.getSchemaId()).equals(schema.getSummary())) {
        LOGGER.warn(
            "schemas {} is changed , won't registry. if want to overwrite schema please upgrade version.",
            schema.getSchemaId());
        LOGGER.warn(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_WARN,
            "schemas is changed , won't registry.", logConstantValue.EVENT_UPDATE));
      }
      localSchemas.remove(schema.getSchemaId());
    });
    return new ArrayList<>(localSchemas.keySet());
  }

  @Override
  public void deregister(ServiceCombRegistration registration) {
    heartbeatScheduler.remove();
    try {
      serviceCombClient
          .deRegisterInstance(RegisterCache.getServiceID(), RegisterCache.getInstanceID());
    } catch (ServiceCombException e) {
      LOGGER.error("deRegisterInstance failed", e);
      LOGGER.error(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_ERROR,
          "deRegisterInstance failed.", logConstantValue.EVENT_CREATE));
    }
  }


  @Override
  public void close() {
    LOGGER.info("close");
    LOGGER.info(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_INFO,
        "close.", logConstantValue.EVENT_CLOSE));
  }

  @Override
  public void setStatus(ServiceCombRegistration registration, String status) {
    try {
      serviceCombClient.updateInstanceStatus(serviceID, instanceID, status);
    } catch (ServiceCombException e) {
      LOGGER.error("setStatus failed", e);
      LOGGER.info(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_INFO,
          "setStatus failed.", logConstantValue.EVENT_CREATE));
    }
  }

  @Override
  public String getStatus(ServiceCombRegistration registration) {
    try {
      MicroserviceInstanceSingleResponse instance = serviceCombClient
          .getInstance(serviceID, instanceID);
      if (instance != null && instance.getInstance() != null) {
        return instance.getInstance().getStatus().name();
      }
    } catch (ServiceCombException e) {
      LOGGER.error("getStatus failed", e);
      LOGGER.info(GenerateLog.generateStructureLog(serviceCombDiscoveryProperties, logConstantValue.LOG_LEVEL_INFO,
          "getStatus failed.", logConstantValue.EVENT_UPDATE));
    }
    return null;
  }


  public String getServiceID() {
    return serviceID;
  }

  public String getInstanceID() {
    return instanceID;
  }
}
