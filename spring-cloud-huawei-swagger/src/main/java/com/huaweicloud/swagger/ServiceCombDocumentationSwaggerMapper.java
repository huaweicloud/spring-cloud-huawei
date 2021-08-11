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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.StringJoiner;

import javax.ws.rs.core.MediaType;

import org.springframework.http.HttpMethod;

import com.google.common.collect.Ordering;

import io.swagger.models.AbstractModel;
import io.swagger.models.Info;
import io.swagger.models.Swagger;
import springfox.documentation.builders.ApiDescriptionBuilder;
import springfox.documentation.builders.ApiListingBuilder;
import springfox.documentation.service.ApiDescription;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.ContentSpecification;
import springfox.documentation.service.Documentation;
import springfox.documentation.service.Operation;
import springfox.documentation.service.Representation;
import springfox.documentation.service.RequestParameter;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.spi.service.contexts.Orderings;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

public class ServiceCombDocumentationSwaggerMapper implements DocumentationSwaggerMapper {
  private static final String TITLE_PREFIX = "swagger definition for ";

  private static final String X_JAVA_INTERFACE_PREFIX = "cse.gen.";

  private static final String X_JAVA_INTERFACE = "x-java-interface";

  private static final String X_JAVA_CLASS = "x-java-class";

  private static final String X_RAW_JSON_TYPE = "x-raw-json";

  private static final String INTF_SUFFIX = "Intf";

  private final ServiceModelToSwagger2Mapper mapper;

  private final String appName;

  private final String serviceName;

  public ServiceCombDocumentationSwaggerMapper(
      String appName, String serviceName, ServiceModelToSwagger2Mapper mapper) {
    this.appName = appName;
    this.serviceName = serviceName;
    this.mapper = mapper;
  }

  @Override
  public Map<String, Swagger> documentationToSwaggers(Documentation documentation) {
    Map<String, Swagger> result = new HashMap<>();

    documentation.getApiListings().entrySet().forEach(entry ->
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
          documentation.getServers(),
          documentation.getExternalDocumentation(),
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
    return new HashSet<>(lists);
  }

  // 02: filtering ApiListings, there are sub tasks
  private Map<String, List<ApiListing>> filteringApiListings(Map.Entry<String, List<ApiListing>> entry) {
    Map<String, List<ApiListing>> map = new HashMap<>();

    List<ApiListing> apiListings = entry.getValue();
    List<ApiListing> filteredApiListing = new ArrayList<>();

    for (ApiListing apiListing : apiListings) {
      ApiListingBuilder apiListingBuilder = new ApiListingBuilder(Ordering.from(Orderings.apiPathCompatator()));
      apiListingBuilder.apiVersion(apiListing.getApiVersion())
          .basePath(apiListing.getBasePath())
          .resourcePath(apiListing.getResourcePath())
          .produces(validateContentType(apiListing.getProduces()
              , MediaType.APPLICATION_JSON)) // 02-02 only keep one produces
          .consumes(validateContentType(apiListing.getConsumes()
              , MediaType.APPLICATION_JSON))// 02-03 only keep one consumers
          .host(apiListing.getHost())
          .protocols(apiListing.getProtocols())
          .securityReferences(apiListing.getSecurityReferences())
          .models(apiListing.getModels())
          .description(apiListing.getDescription())
          .position(apiListing.getPosition())
          .tags(apiListing.getTags())
          .modelSpecifications(apiListing.getModelSpecifications())
          .modelNamesRegistry(apiListing.getModelNamesRegistry());

      List<ApiDescription> apiDescriptions = apiListing.getApis();
      List<ApiDescription> newApiDescriptions = new ArrayList<>(apiDescriptions.size());
      apiDescriptions.forEach(apiDescription -> newApiDescriptions.add(
          new ApiDescriptionBuilder(Ordering.from(Orderings.positionComparator()))
              .path(validatePath(apiDescription.getPath()))
              .description(apiDescription.getDescription())
              // 02-01 only keep the first operation and convert operation.
              .operations(Collections.singletonList(validateOperation(apiDescription.getOperations().get(0))))
              .hidden(apiDescription.isHidden()).build())
      );

      apiListingBuilder.apis(newApiDescriptions);
      filteredApiListing.add(apiListingBuilder.build());
    }

    map.put(entry.getKey(), filteredApiListing);
    return map;
  }

  private String validatePath(String path) {
    if (path.endsWith("/**")) {
      return path.substring(0, path.length() - "/**".length()) + "/(.*)";
    }
    return path;
  }

  @SuppressWarnings({"deprecation", "unchecked"})
  private Operation validateOperation(Operation operation) {
    return new Operation(operation.getMethod(),
        operation.getSummary(),
        operation.getNotes(),
        operation.getExternalDocumentation(),
        operation.getResponseModel(),
        validateOperationId(operation.getMethod(), operation.getUniqueId()),
        operation.getPosition(),
        operation.getTags(),
        validateResponseContentType(operation),
        validateContentType(operation.getConsumes(), MediaType.APPLICATION_JSON),
        operation.getProtocol(),
        Collections.EMPTY_LIST,
        operation.getParameters(),
        operation.getResponseMessages(),
        operation.getDeprecated(),
        operation.isHidden(),
        operation.getVendorExtensions(),
        validateRequestParameter(operation.getRequestParameters()),
        operation.getBody(),
        operation.getResponses()
    );
  }

  private String validateOperationId(HttpMethod method, String uniqueId) {
    String suffix = String.format("Using%s", method);
    if (uniqueId.endsWith(suffix)) {
      return uniqueId.substring(0, uniqueId.length() - suffix.length());
    }
    return uniqueId;
  }

  private Set<RequestParameter> validateRequestParameter(Set<RequestParameter> requestParameters) {
    for (RequestParameter req : requestParameters) {
      if ("body".equals(req.getIn().getIn())) {
        Optional<ContentSpecification> content = req.getParameterSpecification().getContent();
        if (content.isPresent()) {
          for (Representation rep : content.get().getRepresentations()) {
            if (rep.getModel().getScalar().isPresent() && rep.getModel().getScalar().get().getType().getType()
                .equals("string")) {
              req.getExtensions().add(new StringVendorExtension(X_RAW_JSON_TYPE, "true"));
            }
          }
        }
      }
    }
    return requestParameters;
  }

  @SuppressWarnings("deprecation")
  // TODO: fix deprecation problem
  private Set<String> validateResponseContentType(Operation operation) {
    if ("string".equals(operation.getResponseModel().getType())) {
      return validateContentType(operation.getProduces(), MediaType.TEXT_PLAIN);
    }
    return validateContentType(operation.getProduces(), MediaType.APPLICATION_JSON);
  }

  // only keep one produces or consumes
  private Set<String> validateContentType(Set<String> contentTypes, String defaultPrefer) {
    if (contentTypes.isEmpty()) {
      return contentTypes;
    }

    Set<String> onlyOne = new HashSet<>(1);

    if (contentTypes.contains(MediaType.WILDCARD) ||
        contentTypes.contains(defaultPrefer)) {
      onlyOne.add(defaultPrefer);
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
