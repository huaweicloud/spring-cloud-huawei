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

package com.huaweicloud.nacos.config.parser;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.nacos.config.NacosConfigConst;

public abstract class AbstactNacosPropertySourceLoader implements PropertySourceLoader {
  @Override
  public List<PropertySource<?>> load(String name, Resource resource) throws IOException {
    if (resource instanceof NacosByteArrayResource) {
      return doLoad(name, resource);
    }
    return Collections.emptyList();
  }

  protected abstract List<PropertySource<?>> doLoad(String name, Resource resource) throws IOException;

  @SuppressWarnings("unchecked")
  protected void flattenedMap(Map<String, Object> result, Map<String, Object> dataMap, String tempKey) {
    if (CollectionUtils.isEmpty(dataMap)) {
      return;
    }
    for (Map.Entry<String, Object> entry: dataMap.entrySet()) {
      String key = entry.getKey();
      Object value = entry.getValue();
      String currentKey = StringUtils.isEmpty(tempKey) ? key
          : key.startsWith("[") ? tempKey.concat(key) : tempKey.concat(NacosConfigConst.DOT).concat(key);
      if (value instanceof Map) {
        Map<String, Object> map = (Map<String, Object>) value;
        flattenedMap(result, map, currentKey);
        continue;
      } else if (value instanceof Collection) {
        int count = 0;
        Collection<Object> collection = (Collection<Object>) value;
        for (Object object : collection) {
          Map<String, Object> collectionMap = new HashMap<>();
          collectionMap.put("[" + count + "]", object);
          flattenedMap(result, collectionMap, currentKey);
        }
        continue;
      }
      result.put(currentKey, value);
    }
  }
}
