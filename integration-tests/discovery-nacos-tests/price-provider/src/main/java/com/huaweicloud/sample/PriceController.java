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

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

@RestController
public class PriceController {
  @Autowired
  private Configuration configuration;

  @RequestMapping("/price")
  public String sayHello(@RequestParam("id") String id) {
    return id;
  }

  @RequestMapping("/configuration")
  public String getEnums() {
    List<EnumType> enums = configuration.getEnums();

    return enums.toString() + ":" + configuration.getName();
  }

  @RequestMapping("/invocationContext")
  public String invocationContext() {
    InvocationContext invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    if (!"test01".equals(invocationContext.getContext("test01"))) {
      return null;
    }
    if (!"test02".equals(invocationContext.getContext("test02"))) {
      return null;
    }
    if (!"order".equals(invocationContext.getContext(InvocationContext.CONTEXT_MICROSERVICE_NAME))) {
      return null;
    }
    if (StringUtils.isEmpty(invocationContext.getContext(InvocationContext.CONTEXT_INSTANCE_ID))) {
      return null;
    }
    return "success";
  }

  @RequestMapping("/testWebMvcInvocationContext")
  public String testWebMvcInvocationContext(@RequestParam("name") String name) {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    sb.append(".");
    sb.append(context.getContext("x-c"));
    sb.append(".");
    sb.append(context.getContext("x-header-context"));
    sb.append(".");
    sb.append(context.getContext("x-u"));
    sb.append(".");
    sb.append(context.getContext("x-m"));
    return sb.toString();
  }

  @RequestMapping("/priceBalance")
  public String priceBalance(@RequestParam("id") String id) {
    return id;
  }

  @PostMapping("/testPostModel")
  public PojoModel testPostModel(@RequestBody PojoModel model) {
    return model;
  }

  @PostMapping("/testHeaderWithJson")
  public String testHeaderWithJson(@RequestHeader("model") String model) {
    return model;
  }
}
