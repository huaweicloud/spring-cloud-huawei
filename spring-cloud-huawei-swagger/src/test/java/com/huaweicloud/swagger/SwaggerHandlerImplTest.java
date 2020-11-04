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
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Path;
import io.swagger.models.Swagger;
import mockit.Expectations;
import mockit.Injectable;
import mockit.Mocked;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.Documentation;
import springfox.documentation.spring.web.DocumentationCache;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

/**
 * @author GuoYl123
 **/
public class SwaggerHandlerImplTest {

  @Injectable
  private DocumentationCache documentationCache;

  @Injectable
  private ServiceModelToSwagger2Mapper mapper;

  @Injectable
  private ServiceCombClient serviceCombClient;

  @Mocked
  private DefinitionCache def;

  @Injectable
  private ApiListing apiListing;

  private ServiceCombSwaggerHandlerImpl service = new ServiceCombSwaggerHandlerImpl();

  private Multimap<String, ApiListing> mockMap = HashMultimap.create();

  private Documentation mockDoc;

  private Swagger mockSwagger;

  private void init() {
    mockMap.put("xxxx", apiListing);
    mockDoc = new Documentation("xx", "/xx", null, mockMap,
        null, Collections.emptySet(), Collections.emptySet(), null, Collections.emptySet(), Collections.emptyList());
    mockSwagger = new Swagger();
    mockSwagger.setInfo(new Info());
    Map<String, Model> defMap = new HashMap<>();
    defMap.put("xx", new ModelImpl());
    mockSwagger.setDefinitions(defMap);
    Map<String, Path> pathMap = new HashMap<>();
    pathMap.put("xx", new Path());
    mockSwagger.setPaths(pathMap);
    new Expectations() {
      {
        documentationCache.documentationByGroup(anyString);
        result = mockDoc;

        DefinitionCache.getClassNameBySchema(anyString);
        result = "app";

        mapper.mapDocumentation((Documentation) any);
        result = mockSwagger;
      }
    };
  }


  @Test
  public void initTest() {
    init();
    service.documentationCache = documentationCache;
    service.mapper = mapper;
    service.serviceCombClient = serviceCombClient;
    service.init("app", "xx");
    List<String> slist = new ArrayList<>();
    slist.add("xx");
    service.registerSwagger("xx", slist);
    service.withJavaChassis = true;
    service.init("app", "xx");
    service.registerSwagger("xx", slist);
  }
}
