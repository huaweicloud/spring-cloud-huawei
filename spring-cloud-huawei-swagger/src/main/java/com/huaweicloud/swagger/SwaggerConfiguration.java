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

import org.springdoc.core.customizers.SpringDocCustomizers;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.SpringDocProviders;
import org.springdoc.core.service.AbstractRequestService;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.OperationService;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;

@Configuration
public class SwaggerConfiguration {

  @Bean
  @Lazy
  public ServiceCombSwaggerHandler serviceCombSwaggerHandler() {
    return new ServiceCombSwaggerHandlerImpl();
  }

  @Bean
  public OpenApiResourceWrapper openApiResourceWrapper(ObjectFactory<OpenAPIService> openAPIBuilderObjectFactory,
      AbstractRequestService requestBuilder,
      GenericResponseService responseBuilder, OperationService operationParser,
      SpringDocConfigProperties springDocConfigProperties,
      SpringDocProviders springDocProviders,
      SpringDocCustomizers springDocCustomizers) {
    return new OpenApiResourceWrapper(openAPIBuilderObjectFactory, requestBuilder,
        responseBuilder, operationParser, springDocConfigProperties, springDocProviders, springDocCustomizers);
  }
}
