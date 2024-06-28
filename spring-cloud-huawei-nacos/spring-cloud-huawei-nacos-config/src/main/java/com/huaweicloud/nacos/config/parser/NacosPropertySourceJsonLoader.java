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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.boot.env.OriginTrackedMapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.Resource;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.huaweicloud.nacos.config.NacosConfigConst;

public class NacosPropertySourceJsonLoader extends AbstactNacosPropertySourceLoader {
  @Override
  public String[] getFileExtensions() {
    return new String[] { "json" };
  }

  @Override
  @SuppressWarnings("unchecked")
  protected List<PropertySource<?>> doLoad(String name, Resource resource) throws IOException {
    ObjectMapper mapper = new ObjectMapper();
    mapper.configure(Feature.ALLOW_COMMENTS, true);
    Map<String, Object> dataMap = mapper.readValue(resource.getInputStream(), Map.class);
    Map<String, Object> result = new LinkedHashMap<>();
    flattenedMap(result, dataMap, null);
    Map<String, Object> propertySourceMap = rebuildMap(result);
    return Collections.singletonList(new OriginTrackedMapPropertySource(name, propertySourceMap, true));
  }

  private Map<String, Object> rebuildMap(Map<String, Object> map) {
    if (CollectionUtils.isEmpty(map)) {
      return Collections.emptyMap();
    }
    Map<String, Object> resultMap = new LinkedHashMap<>(map);
    for (Map.Entry<String, Object> entry : resultMap.entrySet()) {
      if (entry.getKey().contains(NacosConfigConst.DOT)) {
        int index = entry.getKey().lastIndexOf(NacosConfigConst.DOT);
        if ("value".equalsIgnoreCase(entry.getKey().substring(index + 1))) {
          resultMap.put(entry.getKey().substring(0, index), entry.getValue());
        }
      }
    }
    return resultMap;
  }
}
