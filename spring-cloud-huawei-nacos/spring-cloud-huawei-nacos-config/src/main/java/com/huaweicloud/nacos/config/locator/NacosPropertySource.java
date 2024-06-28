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

package com.huaweicloud.nacos.config.locator;

import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.nacos.config.NacosPropertySourceRepository;

public class NacosPropertySource extends MapPropertySource {
  private final String group;

  private final String dataId;

  private final Date timestamp;

  private final boolean isRefreshable;

  public NacosPropertySource(String group, String dataId, boolean isRefreshable, Map<String, Object> source) {
    super(NacosPropertySourceRepository.getMapKey(dataId, group), source);
    this.timestamp = new Date();
    this.dataId = dataId;
    this.group = group;
    this.isRefreshable = isRefreshable;
  }

  public static Map<String, Object> buildMapSource(List<PropertySource<?>> sources, String dataId, String group) {
    Map<String, Object> sourceMap = new LinkedHashMap<>();
    if (CollectionUtils.isEmpty(sources)) {
      return sourceMap;
    }

    List<PropertySource<?>> otherTypeSources = new ArrayList<>();
    for (PropertySource<?> propertySource : sources) {
      if (propertySource == null) {
        continue;
      }
      if (propertySource instanceof MapPropertySource mapPropertySource) {
        sourceMap.putAll(mapPropertySource.getSource());
      } else {
        otherTypeSources.add(propertySource);
      }
    }
    if (!CollectionUtils.isEmpty(otherTypeSources)) {
      sourceMap.put(NacosPropertySourceRepository.getMapKey(dataId, group), otherTypeSources);
    }
    return sourceMap;
  }

  public String getGroup() {
    return group;
  }

  public String getDataId() {
    return dataId;
  }

  public Date getTimestamp() {
    return timestamp;
  }

  public boolean isRefreshable() {
    return isRefreshable;
  }
}
