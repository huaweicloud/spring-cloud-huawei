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

import java.util.List;
import java.util.Locale;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springdoc.core.AbstractRequestService;
import org.springdoc.core.GenericResponseService;
import org.springdoc.core.OpenAPIService;
import org.springdoc.core.OperationService;
import org.springdoc.core.SpringDocConfigProperties;
import org.springdoc.core.SpringDocProviders;
import org.springdoc.core.customizers.OpenApiCustomiser;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springdoc.core.filters.OpenApiMethodFilter;
import org.springdoc.webmvc.api.OpenApiResource;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.JsonProcessingException;

import io.swagger.v3.oas.models.OpenAPI;

public class SpringMvcOpenApiResource extends OpenApiResource {

    @Autowired
    public SpringMvcOpenApiResource(ObjectFactory<OpenAPIService> openAPIBuilderObjectFactory,
        AbstractRequestService requestBuilder, GenericResponseService responseBuilder, OperationService operationParser,
        Optional<List<OperationCustomizer>> operationCustomizers, Optional<List<OpenApiCustomiser>> openApiCustomisers,
        Optional<List<OpenApiMethodFilter>> methodFilters, SpringDocConfigProperties springDocConfigProperties,
        SpringDocProviders springDocProviders) {
        super(openAPIBuilderObjectFactory, requestBuilder, responseBuilder, operationParser, operationCustomizers,
            openApiCustomisers, methodFilters, springDocConfigProperties, springDocProviders);
    }

    public OpenAPI getOpenAPI() {
        super.initOpenAPIBuilder(Locale.getDefault());
        openAPIService.setServerBaseUrl(getServerBaseUrl());
        OpenAPI openAPI = this.getOpenApi(Locale.getDefault());
        return openAPI;
    }

    @Override
    public String writeYamlValue(OpenAPI openAPI) throws JsonProcessingException {
        return super.writeYamlValue(openAPI);
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
