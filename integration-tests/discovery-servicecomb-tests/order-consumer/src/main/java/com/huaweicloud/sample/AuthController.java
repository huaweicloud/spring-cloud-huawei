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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class AuthController {

  private final RestTemplate restTemplate;

  private final ApplicationContext applicationContext;

  @Autowired
  public AuthController(RestTemplate restTemplate, ApplicationContext applicationContext) {
    this.restTemplate = restTemplate;
    this.applicationContext = applicationContext;
  }

  @RequestMapping("/checkToken")
  public String checkToken() {
    if (applicationContext.containsBean("providerAuthHandler") || !applicationContext.containsBean("authHandlerBoot")) {
      return null;
    }
    return restTemplate.getForObject("http://account-app.account/checkToken", String.class);
  }

  @RequestMapping("/checkTokenSecurity")
  public String checkTokenSecurity() {
    if (applicationContext.containsBean("providerAuthHandler") || !applicationContext.containsBean("authHandlerBoot")) {
      return null;
    }
    return restTemplate.getForObject("http://account-app.account/checkTokenSecurity", String.class);
  }
}
