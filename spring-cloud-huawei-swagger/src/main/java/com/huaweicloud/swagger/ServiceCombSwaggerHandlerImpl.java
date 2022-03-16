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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import io.swagger.models.Swagger;
import io.swagger.v3.core.util.Yaml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.spring.web.plugins.DocumentationPluginsBootstrapper;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class ServiceCombSwaggerHandlerImpl implements ServiceCombSwaggerHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombSwaggerHandlerImpl.class);

  protected DocumentationPluginsBootstrapper documentationPluginsBootstrapper;

  protected DocumentationCache documentationCache;

  protected ServiceModelToSwagger2Mapper mapper;

  private Map<String, Swagger> swaggerMap = new HashMap<>();

  private Map<String, String> swaggerContent = new HashMap<>();

  private Map<String, String> swaggerSummary = new HashMap<>();
  @Autowired
  public ServiceCombSwaggerHandlerImpl(DocumentationPluginsBootstrapper documentationPluginsBootstrapper, DocumentationCache documentationCache, ServiceModelToSwagger2Mapper mapper) {
    this.documentationPluginsBootstrapper = documentationPluginsBootstrapper;
    this.documentationCache = documentationCache;
    this.mapper = mapper;
  }

  @Value("${spring.cloud.servicecomb.swagger.enableJavaChassisAdapter:true}")
  protected boolean withJavaChassis;

  @Override
  public void init(String appName, String serviceName) {
    documentationPluginsBootstrapper.start();

    Documentation documentation = documentationCache
        .documentationByGroup(Docket.DEFAULT_GROUP_NAME);

    DocumentationSwaggerMapper documentationSwaggerMapper;
    if (withJavaChassis) {
      documentationSwaggerMapper = new ServiceCombDocumentationSwaggerMapper(appName, serviceName, mapper);
    } else {
      documentationSwaggerMapper = new SpringCloudDocumentationSwaggerMapper(mapper);
    }
    this.swaggerMap = documentationSwaggerMapper.documentationToSwaggers(documentation);

    this.swaggerContent = calcSchemaContent();

    this.swaggerSummary = calcSchemaSummary();
  }

  private Map<String, String> calcSchemaContent() {
    return swaggerMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
      try {
        return Yaml.mapper().writeValueAsString(entry.getValue());
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
    return swaggerMap.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> {
          try {
            return calcSchemaSummary(Yaml.mapper().writeValueAsString(entry.getValue()));
          } catch (JsonProcessingException e) {
            LOGGER.error("error when calcSchemaSummary.", e);
          }
          return null;
        }));
  }

  private static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }
}
