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
package com.huaweicloud.swagger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import com.huaweicloud.common.log.ServiceCombLogProperties;
import com.huaweicloud.common.log.logConstantValue;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;

import io.swagger.models.Swagger;
import io.swagger.util.Yaml;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

/**
 * @Author GuoYl123
 * @Date 2019/12/17
 **/
public class ServiceCombSwaggerHandlerImpl implements ServiceCombSwaggerHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombSwaggerHandlerImpl.class);

  @Autowired
  protected DocumentationCache documentationCache;

  @Autowired
  protected ServiceModelToSwagger2Mapper mapper;

  @Autowired
  protected ServiceCombClient serviceCombClient;

  @Autowired
  private ServiceCombLogProperties serviceCombLogProperties;

  private Map<String, Swagger> swaggerMap = new HashMap<>();

  @Value("${spring.cloud.servicecomb.swagger.enableJavaChassisAdapter:true}")
  protected boolean withJavaChassis;

  @Override
  public void init(String appName, String serviceName) {
    Documentation documentation = documentationCache
        .documentationByGroup(Docket.DEFAULT_GROUP_NAME);

    if (withJavaChassis) {
      DocumentationSwaggerMapper documentationSwaggerMapper =
          new ServiceCombDocumentationSwaggerMapper(appName, serviceName, mapper);
      this.swaggerMap = documentationSwaggerMapper.documentationToSwaggers(documentation);
    } else {
      DocumentationSwaggerMapper documentationSwaggerMapper = new SpringCloudDocumentationSwaggerMapper(mapper);
      this.swaggerMap = documentationSwaggerMapper.documentationToSwaggers(documentation);
    }
  }

  @Override
  public void registerSwagger(String microserviceId, List<String> schemaIds) {
    schemaIds.forEach(schemaId -> {
      try {
        String str = Yaml.mapper().writeValueAsString(swaggerMap.get(schemaId));
        LOGGER.info("register swagger {}, content: {}{}", schemaId, System.lineSeparator(), str);
        serviceCombClient.registerSchema(microserviceId, schemaId, str);
      } catch (RemoteOperationException e) {
        LOGGER.error("register swagger to server-center failed : {}", e.getMessage());
        LOGGER.error(serviceCombLogProperties.generateStructureLog("register swagger to server-center failed.",
            logConstantValue.LOG_LEVEL_ERROR, logConstantValue.MODULE_CONFIG,
            logConstantValue.EVENT_REGISTER));
      } catch (JsonProcessingException e) {
        LOGGER.error("swagger parse failed : {}", e.getMessage());
        LOGGER.error(serviceCombLogProperties.generateStructureLog("swagger parse failed.",
            logConstantValue.LOG_LEVEL_ERROR, logConstantValue.MODULE_CONFIG,
            logConstantValue.EVENT_REGISTER));
      }
    });
  }

  @Override
  public List<String> getSchemaIds() {
    return new ArrayList<>(swaggerMap.keySet());
  }

  @Override
  public Map<String, String> getSchemasMap() {
    return swaggerMap.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> {
      try {
        return Yaml.mapper().writeValueAsString(entry.getValue());
      } catch (JsonProcessingException e) {
        LOGGER.error("error when calcSchemaSummary.");
      }
      return null;
    }));
  }

  @Override
  public Map<String, String> getSchemasSummaryMap() {
    return swaggerMap.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, entry -> {
          try {
            return calcSchemaSummary(Yaml.mapper().writeValueAsString(entry.getValue()));
          } catch (JsonProcessingException e) {
            LOGGER.error("error when calcSchemaSummary.");
          }
          return null;
        }));
  }

  private static String calcSchemaSummary(String schemaContent) {
    return Hashing.sha256().newHasher().putString(schemaContent, Charsets.UTF_8).hash().toString();
  }
}
