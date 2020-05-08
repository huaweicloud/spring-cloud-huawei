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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.common.base.Charsets;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.hash.Hashing;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import io.swagger.models.AbstractModel;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.AbstractSerializableParameter;
import io.swagger.util.Yaml;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;
import springfox.documentation.service.ApiListing;
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

  private Map<String, Swagger> swaggerMap = new HashMap<>();

  @Value("${spring.cloud.servicecomb.swagger.enableJavaChassisAdapter:true}")
  protected boolean withJavaChassis;

  private String TITLE_PREFIX = "swagger definition for ";

  private String X_JAVA_INTERFACE_PREFIX = "cse.gen.";

  private String X_JAVA_INTERFACE = "x-java-interface";

  private String X_JAVA_CLASS = "x-java-class";

  private String INTF_SUFFIX = "Intf";

  /**
   * Split registration
   */
  public void init(String appName, String serviceName) {
    Documentation documentation = documentationCache
        .documentationByGroup(Docket.DEFAULT_GROUP_NAME);
    if (documentation == null) {
      LOGGER.warn("Unable to find specification for group {}", Docket.DEFAULT_GROUP_NAME);
    }
    Multimap<String, ApiListing> allList = documentation.getApiListings();
    Iterator iter = allList.entries().iterator();
    while (iter.hasNext()) {
      Map.Entry<String, ApiListing> entry = (Map.Entry) iter.next();
      Multimap<String, ApiListing> tempList = HashMultimap.create();
      tempList.put(entry.getKey(), entry.getValue());
      try {
        String fullClassName = DefinitionCache.getFullClassNameBySchema(entry.getKey());
        String className = DefinitionCache.getClassNameBySchema(entry.getKey());
        String xInterfaceName = genXInterfaceName(appName, serviceName, className);
        Field field = Documentation.class.getDeclaredField("apiListings");
        field.setAccessible(true);
        field.set(documentation, tempList);
        Swagger temSwagger = mapper.mapDocumentation(documentation);
        Info info = temSwagger.getInfo();
        info.setTitle(TITLE_PREFIX + fullClassName);
        info.setVendorExtension(X_JAVA_INTERFACE, xInterfaceName);
        temSwagger.setInfo(info);
        temSwagger.setTags(null);
        temSwagger.getDefinitions().forEach((k, v) -> {
          if (v instanceof AbstractModel) {
            ((AbstractModel) v)
                .setVendorExtension(X_JAVA_CLASS, DefinitionCache.getClassByDefName(k));
          }
        });
        if (withJavaChassis) {
          filterSwagger(temSwagger, fullClassName);
          if (CollectionUtils.isEmpty(temSwagger.getPaths())) {
            continue;
          }
          //add text/plain for string
          List contentTypeList = Arrays.asList("text/plain", "application/json");
          temSwagger.getPaths().forEach((path, operation) ->
              operation.getOperations().forEach(api -> {
                api.setConsumes(contentTypeList);
                api.setProduces(contentTypeList);
                api.setTags(Arrays.asList(className));
              })
          );
        }
        swaggerMap.put(className, temSwagger);
      } catch (NoSuchFieldException | IllegalAccessException e) {
        LOGGER.error("parse swagger failed:{}", e.getMessage());
      }
    }
    return;
  }

  /**
   * todo: schema generate also can be async , use aop around method
   * schema注册调用接口可以改为异步,在和java-chassis组网场景下需要同步加载
   *
   * @param microserviceId
   * @param schemaIds
   */
  public void registerSwagger(String microserviceId, List<String> schemaIds) {
    if (withJavaChassis) {
      registerSwaggerSync(microserviceId, schemaIds);
    } else {
      registerSwaggerAsync(microserviceId, schemaIds);
    }
  }

  public List<String> getSchemas() {
    return new ArrayList<>(swaggerMap.keySet());
  }

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

  /**
   * for every method delete UsingGET/UsingXXXX at the end of operationId
   * a method with different httpMethod need be abandoned
   *
   * @param temSwagger
   * @param className
   */
  private void filterSwagger(Swagger temSwagger, String className) {
    Set<String> abandonedList = new HashSet<>();
    Set<String> methodFilter = new HashSet<>();
    temSwagger.getPaths().forEach((k, v) ->
        v.getOperations().forEach(method -> {
          method.getParameters().forEach(parameter -> {
            if (parameter instanceof AbstractSerializableParameter
                && parameter.getIn().equals("query")
                && ((AbstractSerializableParameter) parameter).getType().equals("object")) {
              throw new RuntimeException("class: " + className + " , path: " + k
                  + " object in query can not parse by java chassis , please set enableJavaChassisAdapter");
            }
          });
          String processOptId = method.getOperationId();
          processOptId = processOptId.substring(0, processOptId.indexOf("Using"));
          if (methodFilter.contains(processOptId)) {
            abandonedList.add(k);
            return;
          }
          methodFilter.add(processOptId);
          method.setOperationId(processOptId);
        })
    );
    //todo:exist some springCloud's default API,how to deal them?
    abandonedList.forEach(path -> {
      LOGGER.warn(
          "class: {}, path: {} will not be register swagger,cause it provider multiple http method",
          className, path);
      temSwagger.getPaths().remove(path);
    });
  }

  /**
   * @param microserviceId
   * @param schemaIds
   */
  private void registerSwaggerSync(String microserviceId, List<String> schemaIds) {
    schemaIds.forEach(schemaId -> {
      try {
        String str = Yaml.mapper().writeValueAsString(swaggerMap.get(schemaId));
        LOGGER.info(str);
        serviceCombClient.registerSchema(microserviceId, schemaId, str);
      } catch (RemoteOperationException e) {
        LOGGER.error("register swagger to server-center failed : {}", e.getMessage());
      } catch (JsonProcessingException e) {
        LOGGER.error("swagger parse failed : {}", e.getMessage());
      }
    });
  }

  private void registerSwaggerAsync(String microserviceId, List<String> schemaIds) {
    Executors.newSingleThreadExecutor().execute(() ->
        registerSwaggerSync(microserviceId, schemaIds)
    );
  }

  /**
   * @param appName
   * @param serviceName
   * @param schemaId
   * @return
   */
  private String genXInterfaceName(String appName, String serviceName, String schemaId) {
    return new StringJoiner(".", X_JAVA_INTERFACE_PREFIX, INTF_SUFFIX)
        .add(appName)
        .add(serviceName)
        .add(schemaId)
        .toString();
  }
}
