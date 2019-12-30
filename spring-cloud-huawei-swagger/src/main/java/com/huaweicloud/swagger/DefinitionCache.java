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

import java.util.HashMap;
import java.util.Map;

/**
 * @Author GuoYl123
 * @Date 2019/12/28
 **/
public class DefinitionCache {

  private static Map<String, String> definitionMap = new HashMap<>();

  private static Map<String, String> schemaClassNameMap = new HashMap<>();

  public static String getClassByDefName(String name) {
    return definitionMap.get(name);
  }

  public static void setDefinition(String name, String javaDef) {
    definitionMap.put(name, javaDef);
  }

  public static String getFullClassNameBySchema(String name) {
    return schemaClassNameMap.get(name);
  }

  public static String getClassNameBySchema(String name) {
    String fullName = schemaClassNameMap.get(name);
    return fullName.substring(fullName.lastIndexOf(".") + 1);
  }

  public static void setSchemaClassName(String name, String javaDef) {
    schemaClassNameMap.put(name, javaDef);
  }
}
