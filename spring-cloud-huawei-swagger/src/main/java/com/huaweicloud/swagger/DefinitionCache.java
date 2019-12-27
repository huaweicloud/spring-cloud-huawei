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

  public static String getClassBySchemaName(String name) {
    return schemaClassNameMap.get(name);
  }

  public static void setSchemaClass(String name, String javaDef) {
    schemaClassNameMap.put(name, javaDef);
  }
}
