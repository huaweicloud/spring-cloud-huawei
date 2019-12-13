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

package com.huaweicloud.servicecomb.discovery.client.model;

/**
 * @Author wangqijun
 * @Date 21:45 2019-07-08
 **/

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRootName;

@JsonRootName("service")
public class Microservice {
  private String serviceId;

  private String registerBy;

  private Framework framework;

  private String environment;

  private String appId;

  private String serviceName;

  private String alias;

  private String version;

  private String description;

  private String level;

  private MicroserviceStatus status;

  private List<String> schemas = new ArrayList<String>();

  private List<MicroserviceInstance> instances;

  public List<String> getSchemas() {
    return schemas;
  }

  public void setSchemas(List<String> schemas) {
    this.schemas = schemas;
  }

  public String getServiceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  public String getRegisterBy() {
    return registerBy;
  }

  public void setRegisterBy(String registerBy) {
    this.registerBy = registerBy;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public String getAppId() {
    return appId;
  }

  public void setAppId(String appId) {
    this.appId = appId;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getAlias() {
    return alias;
  }

  public void setAlias(String alias) {
    this.alias = alias;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getDescription() {
    return description;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public MicroserviceStatus getStatus() {
    return status;
  }

  public void setStatus(MicroserviceStatus status) {
    this.status = status;
  }

  public List<MicroserviceInstance> getInstances() {
    return instances;
  }

  public Framework getFramework() {
    return framework;
  }

  public void setFramework(Framework framework) {
    this.framework = framework;
  }

  public void setInstances(
      List<MicroserviceInstance> instances) {
    this.instances = instances;
  }

  @Override
  public String toString() {
    return "Microservice{" +
        "serviceId='" + serviceId + '\'' +
        ", registerBy='" + registerBy + '\'' +
        ", framework=" + framework +
        ", environment='" + environment + '\'' +
        ", appId='" + appId + '\'' +
        ", serviceName='" + serviceName + '\'' +
        ", alias='" + alias + '\'' +
        ", version='" + version + '\'' +
        ", description='" + description + '\'' +
        ", level='" + level + '\'' +
        ", status=" + status +
        ", instances=" + instances +
        '}';
  }
}
