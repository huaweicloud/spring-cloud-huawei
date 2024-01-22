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
package com.huaweicloud.swagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.servicecomb.swagger.SwaggerUtils;
import org.apache.servicecomb.swagger.generator.springmvc.SpringmvcSwaggerGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springdoc.core.Constants;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;

import io.swagger.models.Swagger;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;

public class ServiceCombSwaggerHandlerImpl implements ServiceCombSwaggerHandler, ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombSwaggerHandlerImpl.class);

  private Map<String, OpenAPI> swaggerMap = new HashMap<>();

  private Map<String, String> swaggerContent = new HashMap<>();

  private Map<String, String> swaggerSummary = new HashMap<>();

  private ApplicationContext applicationContext;

  @Value("${spring.cloud.servicecomb.swagger.enableJavaChassisAdapter:true}")
  protected boolean withJavaChassis;

  private OpenApiResourceWrapper openApiResource;

  @Autowired
  public void setOpenApiResource(OpenApiResourceWrapper openApiResource) {
    this.openApiResource = openApiResource;
  }

  @Override
  public void init(String appName, String serviceName) {
    if (withJavaChassis) {
      runJavaChassisScanner();
      return;
    }

    runSpringDocScanner();
  }

  private void runSpringDocScanner() {
    SpringMvcOpenApiResource mvcOpenApiResource = openApiResource.createOpenApiResource(Constants.DEFAULT_GROUP_NAME);
    Set<String> set = mvcOpenApiResource.getControllers();
    set.forEach(key -> {
      SpringMvcOpenApiResource beanOpenApiResource = openApiResource.createOpenApiResource(key);
      beanOpenApiResource.clearCache();
      swaggerMap.put(key, beanOpenApiResource.getOpenAPI());
    });

    renameOperations(swaggerMap);

    this.swaggerContent = calcSchemaContent();

    this.swaggerSummary = calcSchemaSummary();
  }

  private void runJavaChassisScanner() {
    Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
    controllers.forEach((k, v) -> {
      try {
        SpringmvcSwaggerGenerator generator = new SpringmvcSwaggerGenerator(v.getClass());
        Swagger swagger = generator.generate();
        swaggerContent.put(k, SwaggerUtils.swaggerToString(swagger));
        LOGGER.info("generate servicecomb compatible swagger for bean [{}] success.", k);
      } catch (Exception e) {
        LOGGER.info("generate servicecomb compatible swagger for bean [{}] failed, message is [{}].", k,
            e.getMessage());
      }
    });
    swaggerSummary = calcSchemaSummary();
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
    return new ArrayList<>(swaggerContent.keySet());
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

  private void renameOperations(Map<String, OpenAPI> swaggerMap) {
    swaggerMap.forEach((key, openApi) -> {
      openApi.getPaths().forEach((operationID, pathItem) -> {
        AtomicInteger index = new AtomicInteger(0);
        setOperationId(operationID, pathItem.getGet(), index);
        setOperationId(operationID, pathItem.getPut(), index);
        setOperationId(operationID, pathItem.getPost(), index);
        setOperationId(operationID, pathItem.getDelete(), index);
        setOperationId(operationID, pathItem.getOptions(), index);
        setOperationId(operationID, pathItem.getHead(), index);
        setOperationId(operationID, pathItem.getPatch(), index);
        setOperationId(operationID, pathItem.getTrace(), index);
      });
    });
  }

  private void setOperationId(String operationID, Operation operation, AtomicInteger index) {
    if (operation != null) {
      if (index.get() == 0) {
        operation.setOperationId(operationID);
      } else {
        operation.setOperationId(operationID + "_" + index.get());
      }
      index.incrementAndGet();
    }
  }

  private static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }

  @Override
  public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
    this.applicationContext = applicationContext;
  }
}
