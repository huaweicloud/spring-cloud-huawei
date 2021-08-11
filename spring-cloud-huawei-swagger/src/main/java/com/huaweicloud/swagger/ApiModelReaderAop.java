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
import java.util.Set;

import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;

import springfox.documentation.spring.web.scanners.ApiListingScanningContext;

@Aspect
public class ApiModelReaderAop {

  // TODO: fix deprecation problems.
  @SuppressWarnings({"deprecation", "unchecked"})
  @AfterReturning(value = "execution(* springfox.documentation.spring.web.scanners.ApiModelReader.read(..))", returning = "result")
  public void afterDefReturning(Object result) {
    ((Map<String, Set<springfox.documentation.schema.Model>>) result).forEach(
        (key, values) -> {
          values.forEach(value -> {
            DefinitionCache.setDefinition(key, value.getQualifiedType());
          });
        }
    );
  }

  @Before(value = "execution(* springfox.documentation.spring.web.scanners.ApiListingScanner.scan(..)) && args(args)", argNames = "args")
  public void beforeParseSchema(ApiListingScanningContext args) {
    args.getRequestMappingsByResourceGroup().keySet().forEach(k ->
        DefinitionCache.setSchemaClassName(k.getGroupName(), k.getControllerClass().get().getName()));
  }
}
