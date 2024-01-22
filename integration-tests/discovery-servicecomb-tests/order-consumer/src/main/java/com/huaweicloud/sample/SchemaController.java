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

package com.huaweicloud.sample;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

/**
 * Class for testing schema generator
 */
@RestController
public class SchemaController {

  private final ServiceCombSwaggerHandler serviceCombSwaggerHandler;

  private final RestTemplate restTemplate;

  @Autowired
  public SchemaController(ServiceCombSwaggerHandler serviceCombSwaggerHandler, RestTemplate restTemplate) {
    this.serviceCombSwaggerHandler = serviceCombSwaggerHandler;
    this.restTemplate = restTemplate;
  }

  @RequestMapping("/testSchemaGeneratorSpringCloud")
  public String testSchemaGeneratorSpringCloud() {
    return restTemplate.getForObject("http://price/testSchemaGenerator", String.class);
  }

  @RequestMapping("/testSchemaGeneratorServiceComb")
  public String testSchemaGeneratorServiceComb() throws Exception {
    List<String> schemas = serviceCombSwaggerHandler.getSchemaIds();
    Map<String, String> schemaContents = serviceCombSwaggerHandler.getSchemasMap();
    Map<String, String> schemaSummary = serviceCombSwaggerHandler.getSchemasSummaryMap();
    assertThat(schemaContents.size()).isEqualTo(schemas.size());
    assertThat(schemaContents.size()).isEqualTo(schemaSummary.size());

    String a1 = schemaContents.get("schemaContentController");
    String a2 = readFile("SchemaContentController.yaml");
    OpenAPI swagger2 = Yaml.mapper().readValue(a2, OpenAPI.class);
    OpenAPI swagger1 = Yaml.mapper().readValue(a1, OpenAPI.class);
    if (swagger1.equals(swagger2)) {
      return "success";
    } else {
      return a1;
    }
  }

  private String readFile(String restController) {
    // test code, make simple
    try {
      InputStream inputStream = this.getClass().getResource("/" + restController).openStream();
      return IOUtils.toString(inputStream, Charset.forName("UTF-8"));
    } catch (IOException e) {
      Assertions.fail(e.getMessage());
      return null;
    }
  }
}
