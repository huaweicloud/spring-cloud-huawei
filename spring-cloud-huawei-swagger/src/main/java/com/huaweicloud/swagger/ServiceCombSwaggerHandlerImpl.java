/*

  * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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
package com.huaweicloud.swagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;

import io.swagger.v3.oas.models.OpenAPI;

public class ServiceCombSwaggerHandlerImpl implements ServiceCombSwaggerHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombSwaggerHandlerImpl.class);

  private Map<String, OpenAPI> swaggerMap = new HashMap<>();

  private Map<String, String> swaggerContent = new HashMap<>();

  private Map<String, String> swaggerSummary = new HashMap<>();

  @Value("${spring.cloud.servicecomb.swagger.enableJavaChassisAdapter:true}")
  protected boolean withJavaChassis;

  private OpenApiResourceWrapper openApiResource;

  @Autowired
  public void setOpenApiResource(OpenApiResourceWrapper openApiResource) {
    this.openApiResource = openApiResource;
  }

  @Override
  public void init(String appName, String serviceName) {
    swaggerMap.put(Constants.DEFAULT_GROUP_NAME, openApiResource.getOpenApiResource().getOpenAPI());

    if (withJavaChassis) {
      swaggerMap = convertToJavaChassis(swaggerMap);
    }

    this.swaggerContent = calcSchemaContent();

    this.swaggerSummary = calcSchemaSummary();
  }

  private Map<String, OpenAPI> convertToJavaChassis(Map<String, OpenAPI> swaggerMap) {
    //TODO this should be done later to be compatible with java chassis
    return swaggerMap;
  }

  private Map<String, String> calcSchemaContent() {
    return swaggerMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
      try {
        return openApiResource.getOpenApiResource().writeYamlValue(entry.getValue());
      } catch (JsonProcessingException e) {
        LOGGER.error("error when calcSchemaSummary.", e);
      }
      return null;
    }));
  }

  @Override
  public List<String> getSchemaIds() {
    return new ArrayList<>(swaggerMap.keySet());
  }

  @Override
  public Map<String, String> getSchemasMap() {
    return this.swaggerContent;
  }

  @Override
  public Map<String, String> getSchemasSummaryMap() {
    return this.swaggerSummary;
  }

  private Map<String, String> calcSchemaSummary() {
    return swaggerContent.entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> calcSchemaSummary(entry.getValue())));
  }

  private static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }
}
