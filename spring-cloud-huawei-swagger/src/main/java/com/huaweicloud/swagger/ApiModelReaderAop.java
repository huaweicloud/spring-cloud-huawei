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

import java.util.Map;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.util.CollectionUtils;
import springfox.documentation.schema.Model;
import springfox.documentation.spring.web.scanners.ApiListingScanningContext;

/**
 * @Author GuoYl123
 * @Date 2019/12/27
 **/
@Aspect
public class ApiModelReaderAop {

  @AfterReturning(value = "execution(* springfox.documentation.spring.web.scanners.ApiModelReader.read(..))", returning = "result")
  public void afterDefReturning(Object result) {
    Map<String, Model> res = (Map<String, Model>) result;
    if (!CollectionUtils.isEmpty(res)) {
      res.forEach((k, v) -> {
        DefinitionCache.setDefinition(k, v.getQualifiedType());
      });
    }
  }

  @Before(value = "execution(* springfox.documentation.spring.web.scanners.ApiListingScanner.scan(..)) && args(args)", argNames = "args")
  public void beforeParseSchema(ApiListingScanningContext args) {
    args.getRequestMappingsByResourceGroup().forEach((k, v) -> {
      DefinitionCache.setSchemaClassName(k.getGroupName(), k.getControllerClass().get().getName());
    });
  }
}
