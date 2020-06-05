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


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

import io.swagger.models.Swagger;
import springfox.documentation.service.ApiListing;
import springfox.documentation.service.Documentation;
import springfox.documentation.swagger2.mappers.ServiceModelToSwagger2Mapper;

public class ServiceCombDocumentationSwaggerMapper implements DocumentationSwaggerMapper {
  private ServiceModelToSwagger2Mapper mapper;

  public ServiceCombDocumentationSwaggerMapper(ServiceModelToSwagger2Mapper mapper) {
    this.mapper = mapper;
  }

  @Override
  public Map<String, Swagger> documentationToSwaggers(Documentation documentation) {
    Map<String, Swagger> result = new HashMap<>();

    documentation.getApiListings().entries().forEach(entry -> result.put(mapSchemaId(entry.getKey()),
        mapper.mapDocumentation(new Documentation(documentation.getGroupName(),
            documentation.getBasePath(),
            documentation.getTags(),
            toMultiMap(entry),
            documentation.getResourceListing(),
            toSet(documentation.getProduces()),
            toSet(documentation.getConsumes()),
            documentation.getHost(),
            toSet(documentation.getSchemes()),
            documentation.getVendorExtensions()
        ))));
    return result;
  }

  private Set<String> toSet(List<String> lists) {
    return lists.stream().collect(Collectors.toSet());
  }

  private Multimap<String, ApiListing> toMultiMap(Map.Entry<String, ApiListing> entry) {
    Multimap map = HashMultimap.create();
    map.put(entry.getKey(), entry.getValue());
    return map;
  }
}
