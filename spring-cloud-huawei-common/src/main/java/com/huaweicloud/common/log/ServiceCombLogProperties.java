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

package com.huaweicloud.common.log;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huaweicloud.common.cache.RegisterCache;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author zyl
 * @Date 15:12 2020-11-03
 **/
@Component
@ConfigurationProperties("spring.cloud.servicecomb.config")
@ConditionalOnProperty(value = "spring.cloud.servicecomb.discovery.structurelogenabled", matchIfMissing = true)
public class ServiceCombLogProperties {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombLogProperties.class);

  @Value("${spring.cloud.servicecomb.discovery.structurelogenabled:true}")
  private boolean structurelogenabled;

  @Value("${spring.cloud.servicecomb.discovery.serviceName:${spring.application.name:}}")
  private String serviceName;

  @Value("${spring.cloud.servicecomb.discovery.appName:default}")
  private String appName;

  @Value("${spring.cloud.servicecomb.discovery.version:}")
  private String version;

  @Value("${server.env:}")
  private String env;

  public ServiceCombLogProperties() {
  }

  public boolean isStructurelogenabled() {
    return structurelogenabled;
  }

  public void setStructurelogenabled(boolean structurelogenabled) {
    this.structurelogenabled = structurelogenabled;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String generateStructureLog(String msg, String logLevel, String module, String event) {
    if (!this.structurelogenabled) {
      return null;
    }
    StructuredLog log = new StructuredLog();
    long curTime = System.currentTimeMillis();
    log.setLevel(logLevel);
    log.setModule(module);
    log.setEvent(event);
    log.setTimestamp(curTime);
    log.setMsg(msg);
    log.setService(this.getServiceName());
    log.setVersion(this.getVersion());
    log.setEnv(this.getEnv());
    log.setApp(this.getAppName());
    log.setInstance(RegisterCache.getInstanceID());
    log.setSystem(LogConstantValue.SYSTEM_SERVICECOMB);
    try {
      String jasonDataLog  = JsonUtils.OBJ_MAPPER.writeValueAsString(log);
      return jasonDataLog;
    } catch (JsonProcessingException e) {
      LOGGER.error(e.getMessage());
      return null;
    }
  }
}
