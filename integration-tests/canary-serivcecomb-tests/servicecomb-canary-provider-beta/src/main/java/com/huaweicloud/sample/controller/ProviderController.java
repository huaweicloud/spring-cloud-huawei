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
package com.huaweicloud.sample.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletResponse;

@RestController
public class ProviderController {
  private int retryCount = 0;

  @GetMapping("/sayHelloCanary")
  public String sayHelloCanary(@RequestParam("name") String name) {
    return "beta hello------->" + name;
  }

  @GetMapping("/contextSayHelloCanary")
  public String contextSayHelloCanary(@RequestParam("canary") String canary) {
    return "beta hello consumer gateway------->" + canary;
  }

  @GetMapping("retryOnSameZeroCanary")
  public String retryOnSameZeroCanary(HttpServletResponse response) {
    response.setStatus(502);
    return "failed";
  }

  @GetMapping("retryOnSameOneCanary")
  public String retryOnSameOneCanary(HttpServletResponse response) {
    if (retryCount == 1) {
      retryCount = 0;
      return "beta ok";
    }
    response.setStatus(502);
    retryCount++;
    return "failed";
  }

  @GetMapping("testRetryOnSameAllCanary")
  public String testRetryOnSameAllCanary(HttpServletResponse response) {
    response.setStatus(502);
    return "failed";
  }
}
