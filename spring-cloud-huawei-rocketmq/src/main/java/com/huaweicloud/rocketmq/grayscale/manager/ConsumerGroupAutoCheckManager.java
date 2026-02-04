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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.MQClientAPIImpl;
import org.apache.rocketmq.remoting.exception.RemotingConnectException;
import org.apache.rocketmq.remoting.exception.RemotingSendRequestException;
import org.apache.rocketmq.remoting.exception.RemotingTimeoutException;
import org.apache.rocketmq.remoting.protocol.body.GroupList;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;
import org.apache.rocketmq.remoting.protocol.route.BrokerData;
import org.apache.rocketmq.remoting.protocol.route.TopicRouteData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;
import com.huaweicloud.rocketmq.grayscale.config.ConsumeModeEnum;
import com.huaweicloud.rocketmq.grayscale.config.ConsumerClientConfig;

public class ConsumerGroupAutoCheckManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerGroupAutoCheckManager.class);

  private static final AtomicBoolean START_AUTO_CHECK = new AtomicBoolean(false);

  private static final Map<String, ConsumerClientConfig> CONSUMER_CLIENT_CONFIG_MAP = new HashMap<>();

  private static final long ROCKET_MQ_READ_TIMEOUT = 5000L;

  private static final Map<String, HashSet<String>> CONSUMER_GROUP_GRAY_TAG_MAP = new ConcurrentHashMap<>();

  private static ScheduledExecutorService EXECUTOR_SERVICE;

  private static Future<?> future = null;

  public static void addConsumerClientConfig(String consumeScope, ConsumerClientConfig clientConfig) {
    CONSUMER_CLIENT_CONFIG_MAP.putIfAbsent(consumeScope, clientConfig);
  }

  public static ConsumerClientConfig getConsumerClientConfig(String consumeScope) {
    return CONSUMER_CLIENT_CONFIG_MAP.get(consumeScope);
  }

  public static void checkAndStartAutoFindGrayGroup() {
    String grayGroupTags = RocketMqMessageGrayUtils.getGrayGroupTagsByServiceMeta();
    if (!StringUtils.isEmpty(grayGroupTags)) {
      return;
    }
    if (RocketMqMessageGrayUtils.getConsumeMode() == ConsumeModeEnum.AUTO
        && START_AUTO_CHECK.compareAndSet(false, true)) {
      findGrayConsumerGroupAndUpdateGrayTags();
      startSchedulerCheckGroupTask();
      return;
    }
    if (RocketMqMessageGrayUtils.getConsumeMode() == ConsumeModeEnum.BASE) {
      shutdownExecutor();
      START_AUTO_CHECK.compareAndSet(true, false);
    }
  }

  private static synchronized void shutdownExecutor() {
    if (EXECUTOR_SERVICE != null && !EXECUTOR_SERVICE.isShutdown()) {
      try {
        EXECUTOR_SERVICE.shutdown();
        EXECUTOR_SERVICE.awaitTermination(15, TimeUnit.SECONDS);
        future.cancel(true);
      } catch (InterruptedException e) {
        LOGGER.debug("interrupted scheduler task failed!", e);
      }
    }
  }

  public static synchronized void startSchedulerCheckGroupTask() {
    if (EXECUTOR_SERVICE == null || EXECUTOR_SERVICE.isShutdown()) {
      EXECUTOR_SERVICE = Executors.newScheduledThreadPool(1);
      future = EXECUTOR_SERVICE.scheduleWithFixedDelay(
          ConsumerGroupAutoCheckManager::findGrayConsumerGroupAndUpdateGrayTags, 10,
          RocketMqMessageGrayUtils.getAutoCheckDelayTime(), TimeUnit.SECONDS);
    }
  }

  public static void findGrayConsumerGroupAndUpdateGrayTags() {
    if (CONSUMER_CLIENT_CONFIG_MAP.isEmpty()) {
      return;
    }
    for (ConsumerClientConfig clientConfig : CONSUMER_CLIENT_CONFIG_MAP.values()) {
      if (clientConfig.getMqClientInstance() == null) {
        continue;
      }
      Set<String> grayTags = findGrayConsumerGroupAndGetTags(clientConfig);
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("[auto-check] current find gray tags: {}.", grayTags);
      }
      resetAutoFindConsumerGrayTags(grayTags, clientConfig);
    }
  }

  private static Set<String> findGrayConsumerGroupAndGetTags(ConsumerClientConfig clientConfig) {
    try {
      MQClientAPIImpl mqClientApi = clientConfig.getMqClientInstance().getMQClientAPIImpl();
      String brokerAddress = getBrokerAddress(clientConfig.getTopic(), mqClientApi);
      GroupList groupList = mqClientApi.queryTopicConsumeByWho(brokerAddress, clientConfig.getTopic(),
          ROCKET_MQ_READ_TIMEOUT);
      return getGrayTagsByConsumerGroup(groupList, brokerAddress, mqClientApi,
          clientConfig.getConsumerGroup());
    } catch (MQClientException | InterruptedException | RemotingTimeoutException | RemotingSendRequestException
             | RemotingConnectException | MQBrokerException e) {
      LOGGER.error("[auto-check] mqClientApi query consumer group error!", e);
    }
    return new HashSet<>();
  }

  private static String getBrokerAddress(String topic, MQClientAPIImpl mqClientApi)
      throws RemotingSendRequestException, RemotingConnectException, RemotingTimeoutException,
      InterruptedException, MQClientException {
    TopicRouteData topicRouteData = mqClientApi.getTopicRouteInfoFromNameServer(topic, ROCKET_MQ_READ_TIMEOUT, false);
    List<String> brokerList = new ArrayList<>();
    for (BrokerData brokerData : topicRouteData.getBrokerDatas()) {
      brokerList.addAll(brokerData.getBrokerAddrs().values());
    }

    // cluster mode has multiple addresses, just select one
    return CollectionUtils.isEmpty(brokerList) ? "" : brokerList.get(0);
  }

  private static Set<String> getGrayTagsByConsumerGroup(GroupList groupList, String brokerAddress,
      MQClientAPIImpl mqClientApi, String consumerGroup) {
    HashSet<String> grayTags = new HashSet<>();
    for (String group : groupList.getGroupList()) {
      if (group.equals(consumerGroup) || !group.contains(consumerGroup)) {
        continue;
      }
      try {
        List<String> consumerIds = mqClientApi.getConsumerIdListByGroup(brokerAddress, group,
            ROCKET_MQ_READ_TIMEOUT);
        if (consumerIds.isEmpty()) {
          continue;
        }
        String grayTag = StringUtils.substringAfterLast(group, consumerGroup + "_");
        if (!StringUtils.isEmpty(grayTag)) {
          grayTags.add(grayTag);
        }
      } catch (RemotingConnectException | RemotingSendRequestException | RemotingTimeoutException
               | MQBrokerException | InterruptedException e) {
        LOGGER.warn("[auto-check] can not find ids in group: {}.", group);
      }
    }
    return grayTags;
  }

  private static void resetAutoFindConsumerGrayTags(Set<String> grayTags, ConsumerClientConfig clientConfig) {
    String consumeScope = RocketMqMessageGrayUtils.buildCacheKey(clientConfig.getAddress(),
        clientConfig.getTopic(), clientConfig.getConsumerGroup());
    HashSet<String> lastGrayTags = CONSUMER_GROUP_GRAY_TAG_MAP.get(consumeScope);
    if (isConsumerGrayTagsChanged(grayTags, lastGrayTags)) {
      LOGGER.info("consumeScope [{}] grayTags changed, current grayTags [{}], origin grayTags [{}].", consumeScope,
          grayTags, lastGrayTags);
      CONSUMER_GROUP_GRAY_TAG_MAP.put(consumeScope, new HashSet<>(grayTags));
      Map<String, SubscriptionData> subscriptionInner
          = RocketMqConsumerImplManager.getInstance().getSubscriptionInner(consumeScope);
      RocketMqSubscriptionDataManager
          .updateSubscriptionData(subscriptionInner, clientConfig.getTopic(), consumeScope);
    }
  }

  private static boolean isConsumerGrayTagsChanged(Set<String> grayTags, Set<String> lastGrayTags) {
    if (lastGrayTags == null) {
      return !grayTags.isEmpty();
    }
    if (grayTags.isEmpty()) {
      return !lastGrayTags.isEmpty();
    }
    HashSet<String> tempGrayTags = new HashSet<>(grayTags);
    tempGrayTags.removeAll(lastGrayTags);
    return !tempGrayTags.isEmpty() || grayTags.size() != lastGrayTags.size();
  }

  public static List<String> getAutoFindGrayTags(String consumeScope) {
    HashSet<String> consumerGrayTags = CONSUMER_GROUP_GRAY_TAG_MAP.get(consumeScope);
    return consumerGrayTags == null ? new ArrayList<>() : new ArrayList<>(consumerGrayTags);
  }
}
