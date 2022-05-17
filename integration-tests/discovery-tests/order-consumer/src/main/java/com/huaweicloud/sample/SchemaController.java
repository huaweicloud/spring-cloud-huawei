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

package com.huaweicloud.sample;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.assertj.core.api.Assertions;
import org.springdoc.core.Constants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.huaweicloud.common.schema.ServiceCombSwaggerHandler;

import io.swagger.v3.core.util.Yaml;
import io.swagger.v3.oas.models.OpenAPI;

import static org.assertj.core.api.Assertions.assertThat;

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
  public String testSchemaGeneratorServiceComb() throws Exception {
    List<String> schemas = serviceCombSwaggerHandler.getSchemaIds();
    assertThat(schemas.size()).isEqualTo(1);
    Map<String, String> schemaContents = serviceCombSwaggerHandler.getSchemasMap();
    assertThat(schemaContents.size()).isEqualTo(1);

    String a1 = schemaContents.get(Constants.DEFAULT_GROUP_NAME);
    String a2 = readFile("springdocDefault.yaml");

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
      return IOUtils.toString(inputStream);
    } catch (IOException e) {
      Assertions.fail(e.getMessage());
      return null;
    }
  }
}
