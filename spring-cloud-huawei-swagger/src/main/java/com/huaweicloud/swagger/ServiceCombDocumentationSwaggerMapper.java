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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import javax.ws.rs.core.MediaType;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Ordering;

import io.swagger.models.AbstractModel;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import springfox.documentation.builders.ApiDescriptionBuilder;
import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.builders.OperationBuilder;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.Documentation;
import springfox.documentation.service.Operation;
import springfox.documentation.spi.service.contexts.Orderings;
import springfox.documentation.spring.web.readers.operation.CachingOperationNameGenerator;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

public class ServiceCombDocumentationSwaggerMapper implements DocumentationSwaggerMapper {
  private static final String TITLE_PREFIX = "swagger definition for ";

  private static final String X_JAVA_INTERFACE_PREFIX = "cse.gen.";

  private static final String X_JAVA_INTERFACE = "x-java-interface";

  private static final String X_JAVA_CLASS = "x-java-class";

  private static final String INTF_SUFFIX = "Intf";

  private ServiceModelToSwagger2Mapper mapper;

  private String appName;

  private String serviceName;

  public ServiceCombDocumentationSwaggerMapper(
      String appName, String serviceName, ServiceModelToSwagger2Mapper mapper) {
    this.appName = appName;
    this.serviceName = serviceName;
    this.mapper = mapper;
  }

  @Override
  public Map<String, Swagger> documentationToSwaggers(Documentation documentation) {
    Map<String, Swagger> result = new HashMap<>();

    documentation.getApiListings().entries().forEach(entry ->
    {
      Swagger swagger = mapper.mapDocumentation(new Documentation(
          documentation.getGroupName(),
          documentation.getBasePath(),
          Collections.emptySet(), // 01: ignore all tags
          filteringApiListings(entry),  // 02: filtering ApiListings, there are sub tasks
          documentation.getResourceListing(),
          toSet(documentation.getProduces()),
          toSet(documentation.getConsumes()),
          documentation.getHost(),
          toSet(documentation.getSchemes()),
          documentation.getVendorExtensions()
      ));

      changeSwaggerInfo(entry.getKey(), swagger); // 03: change swagger info
      addXJavaClass(swagger);  // 04: add x-java-class

      result.put(mapSchemaId(entry.getKey()), swagger);
    });
    return result;
  }

  private void changeSwaggerInfo(String originalSchemaId, Swagger swagger) {
    String fullClassName = DefinitionCache.getFullClassNameBySchema(originalSchemaId);
    String xInterfaceName = genXInterfaceName(appName, serviceName, mapSchemaId(originalSchemaId));

    Info info = swagger.getInfo();
    info.setTitle(TITLE_PREFIX + fullClassName);
    info.setVendorExtension(X_JAVA_INTERFACE, xInterfaceName);
  }

  private void addXJavaClass(Swagger swagger) {
    swagger.getDefinitions().forEach((k, v) -> {
      if (v instanceof AbstractModel) {
        ((AbstractModel) v)
            .setVendorExtension(X_JAVA_CLASS, DefinitionCache.getClassByDefName(k));
      }
    });
  }

  private Set<String> toSet(List<String> lists) {
    return lists.stream().collect(Collectors.toSet());
  }

  // 02: filtering ApiListings, there are sub tasks
  private Multimap<String, ApiListing> filteringApiListings(Map.Entry<String, ApiListing> entry) {
    Multimap map = HashMultimap.create();
    ApiListing apiListing = entry.getValue();
    ApiListingBuilder apiListingBuilder = new ApiListingBuilder(Ordering.from(Orderings.apiPathCompatator()));
    apiListingBuilder.apiVersion(apiListing.getApiVersion())
        .basePath(apiListing.getBasePath())
        .resourcePath(apiListing.getResourcePath())
        .produces(validateContentType(apiListing.getProduces())) // 02-02 only keep one produces
        .consumes(validateContentType(apiListing.getConsumes()))// 02-03 only keep one consumers
        .host(apiListing.getHost())
        .protocols(apiListing.getProtocols())
        .securityReferences(apiListing.getSecurityReferences())
        .models(apiListing.getModels())
        .description(apiListing.getDescription())
        .position(apiListing.getPosition())
        .tags(apiListing.getTags());

    List<ApiDescription> apiDescriptions = apiListing.getApis();
    List<ApiDescription> newApiDescriptions = new ArrayList<>(apiDescriptions.size());
    apiDescriptions.forEach(apiDescription -> newApiDescriptions.add(
        new ApiDescriptionBuilder(Ordering.from(Orderings.positionComparator()))
            .path(apiDescription.getPath())
            .description(apiDescription.getDescription())
            // 02-01 only keep the first operation and convert operation.
            .operations(Arrays.asList(validateOperation(apiDescription.getOperations().get(0))))
            .hidden(apiDescription.isHidden()).build())
    );

    apiListingBuilder.apis(newApiDescriptions);
    map.put(entry.getKey(), apiListingBuilder.build());
    return map;
  }

  private Operation validateOperation(Operation operation) {
    OperationBuilder builder = new OperationBuilder(new CachingOperationNameGenerator());
    return builder.method(operation.getMethod())
        .summary(operation.getSummary())
        .notes(operation.getNotes())
        .uniqueId(operation.getUniqueId())
        .position(operation.getPosition())
        .produces(validateContentType(operation.getProduces()))
        .consumes(validateContentType(operation.getConsumes()))
        .protocols(operation.getProtocol())
        .parameters(operation.getParameters())
        .responseMessages(operation.getResponseMessages())
        .deprecated(operation.getDeprecated())
        .hidden(operation.isHidden())
        .responseModel(operation.getResponseModel())
        .tags(operation.getTags())
        .extensions(operation.getVendorExtensions()).build();
  }

  // only keep one produces or consumes
  private Set<String> validateContentType(Set<String> contentTypes) {
    if (contentTypes.isEmpty()) {
      return contentTypes;
    }

    Set<String> onlyOne = new HashSet<>(1);

    if (contentTypes.contains(MediaType.WILDCARD)
        || contentTypes.contains(MediaType.APPLICATION_JSON)) {
      onlyOne.add(MediaType.APPLICATION_JSON);
      return onlyOne;
    }

    if (contentTypes.contains(MediaType.TEXT_PLAIN)) {
      onlyOne.add(MediaType.TEXT_PLAIN);
      return onlyOne;
    }

    onlyOne.add(MediaType.APPLICATION_JSON);
    return onlyOne;
  }

  private String genXInterfaceName(String appName, String serviceName, String schemaId) {
    return new StringJoiner(".", X_JAVA_INTERFACE_PREFIX, INTF_SUFFIX)
        .add(appName)
        .add(serviceName)
        .add(schemaId)
        .toString();
  }
}
