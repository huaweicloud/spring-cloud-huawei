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

import org.apache.servicecomb.provider.pojo.Invoker;
import org.apache.servicecomb.provider.rest.common.RestSchema;
import org.apache.servicecomb.provider.springmvc.reference.RestTemplateBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

/**
 * @Author GuoYl123
 * @Date 2019/12/19
 **/
@RestSchema(schemaId = "javachassis-consumer")
@RequestMapping(path = "/consumer")
public class ConsumerController {

  private RestTemplate restTemplate = RestTemplateBuilder.create();

  ProviderService helloService = Invoker
      .createProxy("swagger-provider", "ProviderController", ProviderService.class);

  @GetMapping("/helloFooRT")
  public Foo fooHelloRT(@RequestParam("id") int id) {
    Foo res = restTemplate
        .getForObject("cse://swagger-provider/foo?id=" + id, Foo.class);
    return res;
  }


  @GetMapping("/helloFoo")
  public Foo fooHello(@RequestParam("id") int id) {
    return helloService.foo(id);
  }

  @GetMapping("/hello")
  public String hello(@RequestParam("name") String name) {
    String res = restTemplate
        .getForObject("cse://swagger-provider/hello?name=" + name, String.class);
    return res;
  }

  @GetMapping("/int")
  public int intTest() {
    return helloService.intTest();
  }

  @GetMapping("/invoke")
  public String invoke() {
    return "hello";
  }
}
