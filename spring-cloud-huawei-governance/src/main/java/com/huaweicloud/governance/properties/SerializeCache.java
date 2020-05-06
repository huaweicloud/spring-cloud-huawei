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
package com.huaweicloud.governance.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.util.CollectionUtils;
import org.yaml.snakeyaml.Yaml;

import com.huaweicloud.common.util.MD5Util;

public class SerializeCache<T> {

  Map<String, String> md5Map = new HashMap<>();

  Map<String, T> cacheMap = new HashMap<>();

  public Map<String, T> get(Map<String, String> t, Class<T> entityClass) {
    if (CollectionUtils.isEmpty(t)) {
      return Collections.emptyMap();
    }
    for (Entry<String, String> entry : t.entrySet()) {
      String key = entry.getKey() + entityClass.getName();
      if (!check(key, entry.getValue())) {
        continue;
      }
      Yaml yaml = new Yaml();
      T marker = yaml.loadAs(entry.getValue(), entityClass);
      cacheMap.put(key, marker);
    }
    Map<String, T> resultMap = new HashMap<>();
    for (Entry<String, T> entry : cacheMap.entrySet()) {
      try {
        if (entry.getValue().getClass().isInstance(entityClass.newInstance())) {
          resultMap.put(entry.getKey(), entry.getValue());
        }
      } catch (InstantiationException | IllegalAccessException e) {
        e.printStackTrace();
      }
    }
    return resultMap;
  }

  private boolean check(String key, String value) {
    String md5Str = MD5Util.encrypt(value);
    if (md5Map.containsKey(value) && md5Map.get(value).equals(md5Str)) {
      return false;
    }
    md5Map.put(key, md5Str);
    return true;
  }
}
