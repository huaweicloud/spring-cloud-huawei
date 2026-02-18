/*

 * Copyright (C) 2020-2025 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.rocketmq.grayscale.config;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RocketMqMessageGrayProperties {
  private List<GrayscaleProperties> grayscale = new ArrayList<>();

  private BaseProperties base = new BaseProperties();

  public List<GrayscaleProperties> getGrayscale() {
    return grayscale;
  }

  public void setGrayscale(List<GrayscaleProperties> grayscale) {
    this.grayscale = grayscale;
  }

  public BaseProperties getBase() {
    return base;
  }

  public void setBase(BaseProperties base) {
    this.base = base;
  }

  public Map<String, String> getGrayTagsByServiceMeta(Map<String, String> microServiceMeta) {
    Map<String, String> map = new HashMap<>();
    for (GrayscaleProperties properties : grayscale) {
      if (properties.isServiceMetaMatch(microServiceMeta)
          && !properties.getTrafficTag().isEmpty()) {
        // set item traffic tags when serviceMeta match, because all message tag using traffic tags.
        map.putAll(properties.getTrafficTag());
      }
    }
    return map;
  }

  public Map<String, String> getGrayTagsByGrayHeaders(Map<String, String> trafficGrayHeaders) {
    Map<String, String> map = new HashMap<>();
    for (GrayscaleProperties properties : grayscale) {
      Map.Entry<String, String> matchEntry = properties.getTrafficTagByGrayHeaders(trafficGrayHeaders);
      if (matchEntry != null) {
        map.put(matchEntry.getKey(), matchEntry.getValue());
      }
    }
    return map;
  }
}
