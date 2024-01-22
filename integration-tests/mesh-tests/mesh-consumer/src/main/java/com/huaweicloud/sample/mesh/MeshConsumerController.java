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
package com.huaweicloud.sample.mesh;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/mesh/consumer")
public class MeshConsumerController {
  private final MeshProviderService meshProviderService;

  private final RestTemplate restTemplate;

  @Autowired
  public MeshConsumerController(MeshProviderService meshProviderService,
      RestTemplate restTemplate) {
    this.meshProviderService = meshProviderService;
    this.restTemplate = restTemplate;
  }

  @GetMapping("/sayHelloFeign")
  public String sayHelloFeign(@RequestParam("name") String name) {
    return meshProviderService.sayHello(name);
  }

  @GetMapping("/sayHelloRestTemplate")
  public String sayHelloRestTemplate(@RequestParam("name") String name) {
    return restTemplate.getForObject("http://mesh-provider/mesh/provider/sayHello?name={1}", String.class, name);
  }
}
