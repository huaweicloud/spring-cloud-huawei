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

package com.huaweicloud.rocketmq.grayscale.manager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;
import com.huaweicloud.rocketmq.grayscale.config.ConsumeModeEnum;
import com.huaweicloud.rocketmq.grayscale.config.GrayscaleProperties;

public class RocketMqSubscriptionDataManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqSubscriptionDataManager.class);

  public static final String EXPRESSION_TYPE_TAG = "TAG";

  public static final String EXPRESSION_TYPE_SQL92 = "SQL92";

  public static final String SELECT_ALL_MESSAGE_SQL = "(_message_tag_ is null) or (_message_tag_ is not null)";

  private static final Pattern PATTERN = Pattern.compile(" and | or ", Pattern.CASE_INSENSITIVE);

  private static final String AND_SPLICE_STR = " and ";

  public static void updateSubscriptionData(Map<String, SubscriptionData> subscriptionInner, String topic,
      String consumeScope) {
    if (subscriptionInner == null || subscriptionInner.isEmpty()) {
      return;
    }
    String originSubstring = subscriptionInner.get(topic).getSubString();
    String sql92Substring = buildSql92Substring(subscriptionInner.get(topic), consumeScope);
    for (SubscriptionData subscriptionData : subscriptionInner.values()) {
      subscriptionData.setSubString(sql92Substring);
      if (EXPRESSION_TYPE_TAG.equals(subscriptionData.getExpressionType())) {
        subscriptionData.setExpressionType(EXPRESSION_TYPE_SQL92);
        subscriptionData.getCodeSet().clear();
        subscriptionData.getTagsSet().clear();
      }
      subscriptionData.setSubVersion(System.currentTimeMillis());
    }
    LOGGER.info("update TOPIC: {} SQL92 subscriptionData, originSubStr: {}, sql92SubStr: {}", topic,
        originSubstring, sql92Substring);
  }

  private static String buildSql92Substring(SubscriptionData subscriptionData, String consumeScope) {
    String tempSubStr = subscriptionData.getSubString();
    if (EXPRESSION_TYPE_TAG.equals(subscriptionData.getExpressionType())) {
      tempSubStr = buildTagsSetSql92Expression(subscriptionData.getTagsSet());
    }
    String sql92Expression = addGrayTagsToSql92Expression(tempSubStr, consumeScope);
    if (StringUtils.isEmpty(sql92Expression)) {
      sql92Expression = SELECT_ALL_MESSAGE_SQL;
    }
    return sql92Expression;
  }

  public static String addGrayTagsToSql92Expression(String subscriptionData, String consumeScope) {
    String tempSubscriptionData = subscriptionData;
    if (!StringUtils.isBlank(tempSubscriptionData)) {
      tempSubscriptionData = rebuildSubDataWithoutGrayTag(tempSubscriptionData);
    }
    String sql92Expression = buildGrayTagsSql92Expression(StringUtils.isBlank(tempSubscriptionData), consumeScope);
    if (StringUtils.isBlank(sql92Expression)) {
      return tempSubscriptionData;
    }
    return StringUtils.isBlank(tempSubscriptionData)
        ? sql92Expression : tempSubscriptionData + AND_SPLICE_STR + sql92Expression;
  }

  private static String buildGrayTagsSql92Expression(boolean isOriginSubStrIsEmpty, String consumeScope) {
    StringBuilder sb = new StringBuilder();
    String grayGroupTag = RocketMqMessageGrayUtils.getGrayGroupTagsByServiceMeta();
    if (StringUtils.isEmpty(grayGroupTag)) {
      // base model return without exclude group message
      if (RocketMqMessageGrayUtils.getConsumeMode() == ConsumeModeEnum.BASE) {
        sb.append(buildBaseConsumerSql92Expression(getTrafficTagMapInBaseMode(), isOriginSubStrIsEmpty));
        return sb.toString();
      }

      // auto model return without exclude group and current consume message gray group message
      sb.append(buildBaseConsumerSql92Expression(getTrafficTagMapInAutoMode(consumeScope), isOriginSubStrIsEmpty));
    } else {
      Map<String, List<String>> trafficTagMap = getTrafficTagMapByGroupTags(List.of(grayGroupTag));
      if (!CollectionUtils.isEmpty(trafficTagMap)) {
        sb.append(buildGrayConsumerSql92Expression(trafficTagMap, isOriginSubStrIsEmpty));
      } else {
        LOGGER.warn("current gray group {} had not set grayscale, set it and restart service.", grayGroupTag);
      }
    }
    return sb.toString();
  }

  private static String buildGrayConsumerSql92Expression(Map<String, List<String>> trafficTagMap,
      boolean isOriginSubStrIsEmpty) {
    StringBuilder builder = new StringBuilder();
    if (trafficTagMap.size() > 1 || !isOriginSubStrIsEmpty) {
      builder.append("(");
    }
    for (Map.Entry<String, List<String>> envEntry : trafficTagMap.entrySet()) {
      if (builder.length() > 1) {
        builder.append(" or ");
      }
      builder.append("(")
          .append(envEntry.getKey())
          .append(" in ")
          .append(getStrForSets(new HashSet<>(envEntry.getValue())))
          .append(")");
    }
    if (trafficTagMap.size() > 1 || !isOriginSubStrIsEmpty) {
      builder.append(")");
    }
    return builder.toString();
  }

  private static Map<String, List<String>> getTrafficTagMapInBaseMode() {
    return getTrafficTagMapByGroupTags(RocketMqMessageGrayUtils.getExcludeGroupTags());
  }

  private static Map<String, List<String>> getTrafficTagMapInAutoMode(String consumeScope) {
    List<String> groupTags = new ArrayList<>();
    List<String> excludeGroupTags = RocketMqMessageGrayUtils.getExcludeGroupTags();
    if (!CollectionUtils.isEmpty(excludeGroupTags)) {
      groupTags.addAll(excludeGroupTags);
    }
    List<String> autoFindGroupTags = ConsumerGroupAutoCheckManager.getAutoFindGrayTags(consumeScope);
    if (!CollectionUtils.isEmpty(autoFindGroupTags)) {
      groupTags.addAll(autoFindGroupTags);
    }
    return getTrafficTagMapByGroupTags(groupTags);
  }

  private static String buildBaseConsumerSql92Expression(Map<String, List<String>> trafficTagMap,
      boolean isOriginSubStrIsEmpty) {
    if (CollectionUtils.isEmpty(trafficTagMap)) {
      return "";
    }
    StringBuilder builder = new StringBuilder();
    if (trafficTagMap.size() > 1 || !isOriginSubStrIsEmpty) {
      builder.append("(");
    }
    for (Map.Entry<String, List<String>> envEntry : trafficTagMap.entrySet()) {
      if (builder.length() > 1) {
        builder.append(AND_SPLICE_STR);
      }
      if (trafficTagMap.size() > 1) {
        builder.append("(");
      }
      builder.append("(")
          .append(envEntry.getKey())
          .append(" not in ")
          .append(getStrForSets(new HashSet<>(envEntry.getValue())))
          .append(")")
          .append(" or ")
          .append("(")
          .append(envEntry.getKey())
          .append(" is null")
          .append(")");
      if (trafficTagMap.size() > 1) {
        builder.append(")");
      }
    }
    if (trafficTagMap.size() > 1 || !isOriginSubStrIsEmpty) {
      builder.append(")");
    }
    return builder.toString();
  }

  private static Map<String, List<String>> getTrafficTagMapByGroupTags(List<String> groupTags) {
    Map<String, List<String>> trafficTagMap = new HashMap<>();
    if (CollectionUtils.isEmpty(groupTags)) {
      return trafficTagMap;
    }
    List<GrayscaleProperties> grayscale = RocketMqMessageGrayUtils.getMessageGrayProperties().getGrayscale();
    for (GrayscaleProperties properties : grayscale) {
      if (groupTags.contains(properties.getConsumerGroupTag())) {
        buildTrafficTagMap(trafficTagMap, properties.getTrafficTag());
      }
    }
    return trafficTagMap;
  }

  private static void buildTrafficTagMap(Map<String, List<String>> trafficTagMap, Map<String, String> trafficTag) {
    if (CollectionUtils.isEmpty(trafficTag)) {
      return;
    }
    for (String key : trafficTag.keySet()) {
      trafficTagMap.computeIfAbsent(key, k -> new ArrayList<>()).add(trafficTag.get(key));
    }
  }

  private static String rebuildSubDataWithoutGrayTag(String originSubData) {
    if (StringUtils.isBlank(originSubData)) {
      return originSubData;
    }
    String[] originConditions = PATTERN.split(originSubData);
    List<String> refactorConditions = new ArrayList<>();
    for (String condition : originConditions) {
      if (!containsGrayTags(condition) && !condition.contains("_message_tag_")) {
        refactorConditions.add(condition);
      }
    }
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < refactorConditions.size(); i++) {
      sb.append(refactorConditions.get(i));
      if (i != refactorConditions.size() - 1) {
        sb.append(AND_SPLICE_STR);
      }
    }
    return sb.toString();
  }

  private static boolean containsGrayTags(String condition) {
    for (String key : getGrayTagsSet()) {
      if (condition.contains(key)) {
        return true;
      }
    }
    return false;
  }

  private static Set<String> getGrayTagsSet() {
    Set<String> grayTags = new HashSet<>();
    for (GrayscaleProperties item : RocketMqMessageGrayUtils.getMessageGrayProperties().getGrayscale()) {
      if (!item.getTrafficTag().isEmpty()) {
        grayTags.addAll(item.getTrafficTag().keySet());
      }
    }
    return grayTags;
  }

  public static String buildTagsSetSql92Expression(Set<String> tagsSet) {
    return tagsSet != null && !tagsSet.isEmpty() ? buildTagsExpression(tagsSet) : "";
  }

  private static String buildTagsExpression(Set<String> tagsSet) {
    return "(TAGS is not null and TAGS in " + getStrForSets(tagsSet) + ")";
  }

  private static String getStrForSets(Set<String> tags) {
    StringBuilder builder = new StringBuilder("(");
    for (String tag : tags) {
      builder.append("'").append(tag).append("'");
      builder.append(",");
    }
    builder.deleteCharAt(builder.length() - 1);
    builder.append(")");
    return builder.toString();
  }
}
