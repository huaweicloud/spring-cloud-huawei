/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huaweicloud.service.engine.common.configration.bootstrap;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

public class MicroserviceProperties {
  @Value("${spring.cloud.servicecomb.service.application:"
      + "${spring.cloud.servicecomb.discovery.appName:default}}")
  private String application;

  @Value("${spring.cloud.servicecomb.service.name:"
      + "${spring.cloud.servicecomb.discovery.serviceName:"
      + "${spring.application.name:defaultMicroservice}}}")
  private String name;

  @Value("${spring.cloud.servicecomb.service.environment:"
      + "${server.env:}}")
  private String environment;

  @Value("${spring.cloud.servicecomb.service.version:"
      + "${spring.cloud.servicecomb.discovery.version:1.0.0.0}}")
  private String version;

  private String description;

  // should be writable
  private Map<String, String> properties = new HashMap<>();

  public String getProperty(String key) {
    return properties.get(key);
  }

  public void addProperty(String key, String value) {
    properties.put(key, value);
  }

  public String getApplication() {
    return application;
  }

  public void setApplication(String application) {
    this.application = application;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
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

  public Map<String, String> getProperties() {
    return properties;
  }

  public void setProperties(Map<String, String> properties) {
    this.properties = properties;
  }
}
