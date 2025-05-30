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

package com.huaweicloud.rocketmq.grayscale;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.rocketmq.grayscale.config.ConsumeModeEnum;
import com.huaweicloud.rocketmq.grayscale.config.GrayscaleProperties;
import com.huaweicloud.rocketmq.grayscale.config.RocketMqMessageGrayProperties;

public class RocketMqMessageGrayUtils {
  private static final Map<String, String> MICRO_SERVICE_PROPERTIES = new HashMap<>();

  private static RocketMqMessageGrayProperties messageGrayProperties = new RocketMqMessageGrayProperties();

  public static void setServiceMetaData(Map<String, String> serviceMeta) {
    MICRO_SERVICE_PROPERTIES.putAll(serviceMeta);
  }

  public static void setMessageGrayProperties(RocketMqMessageGrayProperties grayProperties) {
    messageGrayProperties = grayProperties;
  }

  public static String getGrayGroupTagsByServiceMeta() {
    if (MICRO_SERVICE_PROPERTIES.isEmpty()) {
      return "";
    }
    for (GrayscaleProperties item : messageGrayProperties.getGrayscale()) {
      Map<String, String> ruleServiceMeta = item.getServiceMeta();
      for (Map.Entry<String, String> entry : MICRO_SERVICE_PROPERTIES.entrySet()) {
        if (ruleServiceMeta.containsKey(entry.getKey())
            && StringUtils.equals(ruleServiceMeta.get(entry.getKey()), entry.getValue())) {
          return item.getConsumerGroupTag();
        }
      }
    }
    return "";
  }

  public static RocketMqMessageGrayProperties getMessageGrayProperties() {
    return messageGrayProperties;
  }

  public static long getAutoCheckDelayTime() {
    return messageGrayProperties.getBase().getAutoCheckDelayTime();
  }

  public static ConsumeModeEnum getConsumeMode() {
    return messageGrayProperties.getBase().getConsumeMode();
  }

  public static List<String> getExcludeGroupTags() {
    return messageGrayProperties.getBase().getExcludeGroupTags();
  }

  public static Map<String, String> getGrayTagsByServiceMeta() {
    return messageGrayProperties.getGrayTagsByServiceMeta(MICRO_SERVICE_PROPERTIES);
  }

  public static Map<String, String> getGrayTagsByGrayHeaders(Map<String, String> trafficGrayHeaders) {
    return messageGrayProperties.getGrayTagsByGrayHeaders(trafficGrayHeaders);
  }

  public static Map<String, HashSet<String>> getAllTrafficTagMap() {
    Map<String, HashSet<String>> trafficTags = new HashMap<>();
    for (GrayscaleProperties grayscale : messageGrayProperties.getGrayscale()) {
      for (String key : grayscale.getTrafficTag().keySet()) {
        if (trafficTags.get(key) != null) {
          trafficTags.get(key).add(grayscale.getTrafficTag().get(key));
        } else {
          HashSet<String> values = new HashSet<>();
          values.add(grayscale.getTrafficTag().get(key));
          trafficTags.put(key, values);
        }
      }
    }
    return trafficTags;
  }

  public static String getGrayConsumerGroup(String consumerGroup) {
    String grayGroupTag = getGrayGroupTagsByServiceMeta();
    if (StringUtils.isEmpty(grayGroupTag)) {
      return consumerGroup;
    }
    String grayGroupSuffix = "_" + grayGroupTag;
    if (consumerGroup.endsWith(grayGroupSuffix)) {
      return consumerGroup;
    }
    return consumerGroup + grayGroupSuffix;
  }

  public static String buildCacheKey(String address, String topic, String consumerGroup) {
    return address + "@" + topic + "@" + consumerGroup;
  }

  public static void setInvocationContext(Map<String, String> properties) {
    Map<String, HashSet<String>> trafficTagMap = getAllTrafficTagMap();
    if (CollectionUtils.isEmpty(properties) || CollectionUtils.isEmpty(trafficTagMap)) {
      return;
    }
    InvocationContext invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    for (String key : properties.keySet()) {
      if (trafficTagMap.get(key) != null && properties.get(key) != null
          && trafficTagMap.get(key).contains(properties.get(key))) {
        invocationContext.putContext(key, properties.get(key));
      }
    }
  }
}
