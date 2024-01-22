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
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;

/**
 * @Author GuoYl123
 * @Date 2019/12/17
 **/
@Configuration
public class SwaggerConfiguration {

  @Bean
  @Lazy
  public ServiceCombSwaggerHandler serviceCombSwaggerHandler() {
    return new ServiceCombSwaggerHandlerImpl();
  }

  @Bean
  OpenApiResourceWrapper openApiResourceWrapper(ObjectFactory<OpenAPIService> openAPIBuilderObjectFactory,
      AbstractRequestService requestBuilder,
      GenericResponseService responseBuilder, OperationService operationParser,
      SpringDocConfigProperties springDocConfigProperties,
      Optional<List<OperationCustomizer>> operationCustomizers,
      Optional<List<OpenApiCustomiser>> openApiCustomisers,
      Optional<List<OpenApiMethodFilter>> methodFilters,
      SpringDocProviders springDocProviders) {
    return new OpenApiResourceWrapper(openAPIBuilderObjectFactory, requestBuilder,
        responseBuilder, operationParser, operationCustomizers,
        openApiCustomisers, methodFilters, springDocConfigProperties, springDocProviders);
  }
}
