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

package com.huaweicloud.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;

/**
 * Class for testing schema generator
 */
@RestController
public class SchemaController {
  @Autowired
  ServiceCombSwaggerHandler serviceCombSwaggerHandler;

  @Autowired
  private RestTemplate restTemplate;

  @RequestMapping("/testSchemaGeneratorSpringCloud")
  public String testSchemaGeneratorSpringCloud() {
    return restTemplate.getForObject("http://price/testSchemaGenerator", String.class);
  }

  @RequestMapping("/testSchemaGeneratorServiceComb")
  public String testSchemaGeneratorServiceComb() {
    List<String> schemas = serviceCombSwaggerHandler.getSchemaIds();
    assertThat(schemas.size()).isGreaterThan(3);
    Map<String, String> schemaContents = serviceCombSwaggerHandler.getSchemasMap();
    assertThat(schemaContents.size()).isGreaterThan(3);
    // TODO : add other test case for swagger content
    return "success";
  }
}
