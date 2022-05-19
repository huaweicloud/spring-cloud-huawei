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
import java.util.Set;
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

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Paths;

public class ServiceCombSwaggerHandlerImpl implements ServiceCombSwaggerHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombSwaggerHandlerImpl.class);

  private Map<String, OpenAPI> swaggerMap = new HashMap<>();

  private Map<String, String> swaggerContent = new HashMap<>();

  private Map<String, String> swaggerSummary = new HashMap<>();

  private Map<String, SpringMvcOpenApiResource> apiMap = new HashMap<>();

  @Value("${spring.cloud.servicecomb.swagger.enableJavaChassisAdapter:true}")
  protected boolean withJavaChassis;

  private OpenApiResourceWrapper openApiResource;

  @Autowired
  public void setOpenApiResource(OpenApiResourceWrapper openApiResource) {
    this.openApiResource = openApiResource;
  }

  @Override
  public void init(String appName, String serviceName) {
    SpringMvcOpenApiResource mvcOpenApiResource = openApiResource.createOpenApiResource(Constants.DEFAULT_GROUP_NAME);
    //mvcOpenApiResource.getOpenAPI();
    //apiMap.put(Constants.DEFAULT_GROUP_NAME, mvcOpenApiResource);
    //swaggerMap.put(Constants.DEFAULT_GROUP_NAME, mvcOpenApiResource.getOpenAPI());

    Set<String> set = mvcOpenApiResource.getControllers();
    set.forEach(key -> {
      SpringMvcOpenApiResource beanOpenApiResource = openApiResource.createOpenApiResource(key);
      beanOpenApiResource.clearCache();
      apiMap.put(key, beanOpenApiResource);
      swaggerMap.put(key, beanOpenApiResource.getOpenAPI());
    });

    renameOperations(swaggerMap);
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
    return swaggerContent.entrySet()
        .stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> calcSchemaSummary(entry.getValue())));
  }

  private void renameOperations(Map<String, OpenAPI> swaggerMap) {
    swaggerMap.forEach((key, openApi) -> {
      openApi.getPaths().forEach((operationID, pathItem) -> {
        int index = 0;
        if (pathItem.getGet() != null) {
          if (index == 0) {
            pathItem.getGet().setOperationId(operationID);
          } else {
            pathItem.getGet().setOperationId(operationID + "_" + index);
          }
          index++;
        }
        if (pathItem.getPut() != null) {
          if (index == 0) {
            pathItem.getPut().setOperationId(operationID);
          } else {
            pathItem.getPut().setOperationId(operationID + "_" + index);
          }
          index++;
        }
        if (pathItem.getPost() != null) {
          if (index == 0) {
            pathItem.getPost().setOperationId(operationID);
          } else {
            pathItem.getPost().setOperationId(operationID + "_" + index);
          }
          index++;
        }
        if (pathItem.getDelete() != null) {
          if (index == 0) {
            pathItem.getDelete().setOperationId(operationID);
          } else {
            pathItem.getDelete().setOperationId(operationID + "_" + index);
          }
          index++;
        }
        if (pathItem.getOptions() != null) {
          if (index == 0) {
            pathItem.getOptions().setOperationId(operationID);
          } else {
            pathItem.getOptions().setOperationId(operationID + "_" + index);
          }
          index++;
        }
        if (pathItem.getHead() != null) {
          if (index == 0) {
            pathItem.getHead().setOperationId(operationID);
          } else {
            pathItem.getHead().setOperationId(operationID + "_" + index);
          }
          index++;
        }
        if (pathItem.getPatch() != null) {
          if (index == 0) {
            pathItem.getPatch().setOperationId(operationID);
          } else {
            pathItem.getPatch().setOperationId(operationID + "_" + index);
          }
          index++;
        }
        if (pathItem.getTrace() != null) {
          if (index == 0) {
            pathItem.getTrace().setOperationId(operationID);
          } else {
            pathItem.getTrace().setOperationId(operationID + "_" + index);
          }
          index++;
        }
      });
    });
  }

  private static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }
}
