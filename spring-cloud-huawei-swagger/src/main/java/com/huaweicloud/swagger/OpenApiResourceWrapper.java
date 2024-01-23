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

import java.util.List;
import java.util.Optional;

import org.springdoc.core.AbstractRequestService;
import org.springdoc.core.GenericResponseService;
import org.springdoc.core.OpenAPIService;
import org.springdoc.core.OperationService;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocProviders;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.filters.OpenApiMethodFilter;
import org.springframework.beans.factory.ObjectFactory;

/**
 * This class is to create a SpringMvcOpenApiResource object,
 * if make SpringMvcOpenApiResource as a spring bean,it will effect springdoc
 */
public class OpenApiResourceWrapper {

  ObjectFactory<OpenAPIService> openAPIBuilderObjectFactory;

  AbstractRequestService requestBuilder;

  GenericResponseService responseBuilder;

  OperationService operationParser;

  Optional<List<OperationCustomizer>> operationCustomizers;

  Optional<List<OpenApiCustomiser>> openApiCustomisers;

  Optional<List<OpenApiMethodFilter>> methodFilters;

  SpringDocConfigProperties springDocConfigProperties;

  SpringDocProviders springDocProviders;

  public OpenApiResourceWrapper(
      ObjectFactory<OpenAPIService> openAPIBuilderObjectFactory, AbstractRequestService requestBuilder,
      GenericResponseService responseBuilder, OperationService operationParser,
      Optional<List<OperationCustomizer>> operationCustomizers,
      Optional<List<OpenApiCustomiser>> openApiCustomisers,
      Optional<List<OpenApiMethodFilter>> methodFilters,
      SpringDocConfigProperties springDocConfigProperties, SpringDocProviders springDocProviders) {
    this.openAPIBuilderObjectFactory = openAPIBuilderObjectFactory;
    this.requestBuilder = requestBuilder;
    this.responseBuilder = responseBuilder;
    this.operationParser = operationParser;
    this.operationCustomizers = operationCustomizers;
    this.openApiCustomisers = openApiCustomisers;
    this.methodFilters = methodFilters;
    this.springDocConfigProperties = springDocConfigProperties;
    this.springDocProviders = springDocProviders;
  }

  public SpringMvcOpenApiResource createOpenApiResource(String groupName) {
    return new SpringMvcOpenApiResource(groupName, openAPIBuilderObjectFactory, requestBuilder,
        responseBuilder, operationParser, operationCustomizers,
        openApiCustomisers, methodFilters, springDocConfigProperties, springDocProviders);
  }
}
