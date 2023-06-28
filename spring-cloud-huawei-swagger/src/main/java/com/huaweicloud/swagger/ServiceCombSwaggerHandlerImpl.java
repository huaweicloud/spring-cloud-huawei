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
import java.util.LinkedHashMap;
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

import io.swagger.models.Info;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.Paths;

public class ServiceCombSwaggerHandlerImpl implements ServiceCombSwaggerHandler, ApplicationContextAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombSwaggerHandlerImpl.class);

  private Map<String, OpenAPI> swaggerMap = new HashMap<>();

  private List<OpenAPI> openAPIList = new ArrayList<>();

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
      runJavaChassisScanner(serviceName);
      return;
    }

    runSpringDocScanner(serviceName);
  }

  private void runSpringDocScanner(String serviceName) {
    SpringMvcOpenApiResource mvcOpenApiResource = openApiResource.createOpenApiResource(Constants.DEFAULT_GROUP_NAME);
    Set<String> set = mvcOpenApiResource.getControllers();
    set.forEach(key -> {
      SpringMvcOpenApiResource beanOpenApiResource = openApiResource.createOpenApiResource(key);
      beanOpenApiResource.clearCache();
      openAPIList.add(beanOpenApiResource.getOpenAPI());
    });
    renameOperations(serviceName, openAPIList);

    this.swaggerContent = calcSchemaContent();

    this.swaggerSummary = calcSchemaSummary();
  }

  private void runJavaChassisScanner(String serviceName) {
    Map<String, Object> controllers = applicationContext.getBeansWithAnnotation(RestController.class);
    Swagger swaggerMerge = new Swagger();
    controllers.forEach((k, v) -> {
      try {
        SpringmvcSwaggerGenerator generator = new SpringmvcSwaggerGenerator(v.getClass());
        Swagger swagger = generator.generate();
        if (swaggerMerge.getPaths() == null) {
          Info info = new Info().version("1.0.0");
          info.setTitle("swagger definition for " + serviceName);
          swaggerMerge.setInfo(info);
          swaggerMerge.setPaths(swagger.getPaths());
          swaggerMerge.setBasePath(swagger.getBasePath());
          swaggerMerge.setConsumes(swagger.getConsumes());
          swaggerMerge.setProduces(swagger.getProduces());
          swaggerMerge.setPaths(convertWithTags(swagger.getPaths(), k));
        } else {
          swaggerMerge.getPaths().putAll(convertWithTags(swagger.getPaths(), k));
        }
        LOGGER.info("generate servicecomb compatible swagger for bean [{}] success.", k);
      } catch (Exception e) {
        LOGGER.info("generate servicecomb compatible swagger for bean [{}] failed, message is [{}].", k,
            e.getMessage());
      }
    });
    swaggerContent.put(serviceName, SwaggerUtils.swaggerToString(swaggerMerge));
    swaggerSummary = calcSchemaSummary();
  }

  private Map<String, Path> convertWithTags(Map<String, Path> paths, String className) {
    paths.entrySet().forEach((entry) -> {
      entry.getValue().getVendorExtensions().put("schemaId", className);
    });
    return paths;
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

  private void renameOperations(String serviceName, List<OpenAPI> openAPIList) {
    OpenAPI openAPIMerge = new OpenAPI();
    openAPIMerge.servers(openAPIList.get(0).getServers());
    openAPIMerge.setInfo(openAPIList.get(0).getInfo());
    openAPIMerge.setComponents(openAPIList.get(0).getComponents());
    openAPIMerge.setPaths(new Paths());
    openAPIList.forEach((openApi) -> {
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
      openAPIMerge.getPaths().putAll(openApi.getPaths());
    });
    swaggerMap.put(serviceName, openAPIMerge);
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
