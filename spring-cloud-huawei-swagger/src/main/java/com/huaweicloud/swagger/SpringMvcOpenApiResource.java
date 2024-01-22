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

import static org.springdoc.core.utils.Constants.DEFAULT_GROUP_NAME;

import java.util.Locale;
import java.util.Set;

import org.springdoc.core.customizers.SpringDocCustomizers;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.providers.SpringDocProviders;
import org.springdoc.core.service.AbstractRequestService;
import org.springdoc.core.service.GenericResponseService;
import org.springdoc.core.service.OpenAPIService;
import org.springdoc.core.service.OperationService;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.web.method.HandlerMethod;

import io.swagger.v3.oas.models.OpenAPI;
import jakarta.servlet.http.HttpServletRequest;

public class SpringMvcOpenApiResource extends OpenApiResource {

  public SpringMvcOpenApiResource(String groupName, ObjectFactory<OpenAPIService> openAPIBuilderObjectFactory,
      AbstractRequestService requestBuilder, GenericResponseService responseBuilder,
      OperationService operationParser, SpringDocConfigProperties springDocConfigProperties,
      SpringDocProviders springDocProviders, SpringDocCustomizers springDocCustomizers) {
    super(groupName, openAPIBuilderObjectFactory, requestBuilder, responseBuilder, operationParser,
        springDocConfigProperties,
        springDocProviders, springDocCustomizers);
  }

  public OpenAPI getOpenAPI() {
    super.initOpenAPIBuilder(Locale.getDefault());
    openAPIService.setServerBaseUrl(getServerBaseUrl());
    return this.getOpenApi(Locale.getDefault());
  }

  @Override
  protected boolean isFilterCondition(HandlerMethod handlerMethod, String operationPath, String[] produces,
      String[] consumes, String[] headers) {
    if (DEFAULT_GROUP_NAME.equals(groupName)) {
      return super.isFilterCondition(handlerMethod, operationPath, produces, consumes, headers);
    }
    return super.isFilterCondition(handlerMethod, operationPath, produces, consumes, headers)
        && handlerMethod.getBean().equals(groupName);
  }

  public void clearCache() {
    openAPIService.setCachedOpenAPI(null, Locale.getDefault());
  }

  public Set<String> getControllers() {
    openAPIService.build(Locale.getDefault());
    return openAPIService.getMappingsMap().keySet();
  }

  /**
   * Implement an empty method,this method will not be used.
   *
   * @param request    the request
   * @param apiDocsUrl the api docs url
   * @return the server url
   */
  @Override
  protected String getServerUrl(HttpServletRequest request, String apiDocsUrl) {
    return null;
  }

  private String getServerBaseUrl() {
    return "/";
  }
}
