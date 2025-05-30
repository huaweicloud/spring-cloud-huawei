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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

public class GrayscaleProperties {
  private String consumerGroupTag;

  private Map<String, String> serviceMeta = new HashMap<>();

  private Map<String, String> trafficTag = new HashMap<>();

  public String getConsumerGroupTag() {
    return consumerGroupTag;
  }

  public void setConsumerGroupTag(String consumerGroupTag) {
    this.consumerGroupTag = consumerGroupTag;
  }

  public Map<String, String> getServiceMeta() {
    return serviceMeta;
  }

  public void setServiceMeta(Map<String, String> serviceMeta) {
    this.serviceMeta = serviceMeta;
  }

  public Map<String, String> getTrafficTag() {
    return trafficTag;
  }

  public void setTrafficTag(Map<String, String> trafficTag) {
    this.trafficTag = trafficTag;
  }

  public boolean isServiceMetaMatch(Map<String, String> microServiceMeta) {
    for (Map.Entry<String, String> entry : microServiceMeta.entrySet()) {
      if (serviceMeta.containsKey(entry.getKey())
          && StringUtils.equals(serviceMeta.get(entry.getKey()), entry.getValue())) {
        return true;
      }
    }
    return false;
  }

  public Map.Entry<String, String> getTrafficTagByGrayHeaders(Map<String, String> trafficGrayHeaders) {
    for (Map.Entry<String, String> entry : trafficGrayHeaders.entrySet()) {
      if (trafficTag.containsKey(entry.getKey())
          && trafficTag.get(entry.getKey()).equals(entry.getValue())) {
        return entry;
      }
    }
    return null;
  }
}
