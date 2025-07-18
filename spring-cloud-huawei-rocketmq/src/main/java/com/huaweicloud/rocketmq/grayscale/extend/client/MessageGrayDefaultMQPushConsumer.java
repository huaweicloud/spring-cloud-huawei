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

package com.huaweicloud.rocketmq.grayscale.extend.client;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.AllocateMessageQueueStrategy;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.consumer.MessageSelector;
import org.apache.rocketmq.client.consumer.rebalance.AllocateMessageQueueAveragely;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.RPCHook;

import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;
import com.huaweicloud.rocketmq.grayscale.config.ConsumerClientConfig;
import com.huaweicloud.rocketmq.grayscale.manager.ConsumerGroupAutoCheckManager;
import com.huaweicloud.rocketmq.grayscale.manager.RocketMqConsumerImplManager;
import com.huaweicloud.rocketmq.grayscale.manager.RocketMqSubscriptionDataManager;

public class MessageGrayDefaultMQPushConsumer extends DefaultMQPushConsumer {
  private String topic;

  public MessageGrayDefaultMQPushConsumer(final String consumerGroup) {
    this(consumerGroup, null, new AllocateMessageQueueAveragely());
  }

  public MessageGrayDefaultMQPushConsumer(RPCHook rpcHook) {
    this(MixAll.DEFAULT_CONSUMER_GROUP, rpcHook, new AllocateMessageQueueAveragely());
  }

  public MessageGrayDefaultMQPushConsumer(final String consumerGroup, RPCHook rpcHook) {
    this(consumerGroup, rpcHook, new AllocateMessageQueueAveragely());
  }

  public MessageGrayDefaultMQPushConsumer(final String consumerGroup, boolean enableMsgTrace,
      final String customizedTraceTopic) {
    this(consumerGroup, null, new AllocateMessageQueueAveragely(), enableMsgTrace, customizedTraceTopic);
  }

  public MessageGrayDefaultMQPushConsumer(final String consumerGroup, RPCHook rpcHook,
      AllocateMessageQueueStrategy allocateMessageQueueStrategy) {
    this(consumerGroup, rpcHook, allocateMessageQueueStrategy, false, null);
  }

  private MessageGrayDefaultMQPushConsumer(final String consumerGroup, RPCHook rpcHook,
      AllocateMessageQueueStrategy allocateMessageQueueStrategy, boolean enableMsgTrace,
      final String customizedTraceTopic) {
    super(consumerGroup, rpcHook, allocateMessageQueueStrategy, enableMsgTrace, customizedTraceTopic);
    checkAndSetConsumerGroup(consumerGroup);
  }

  private void checkAndSetConsumerGroup(String consumerGroup) {
    String grayConsumerGroup = RocketMqMessageGrayUtils.getGrayConsumerGroup(consumerGroup);
    if (StringUtils.equals(consumerGroup, grayConsumerGroup)) {
      return;
    }
    setConsumerGroup(grayConsumerGroup);
  }

  @Override
  public void subscribe(String topic, String subExpression) throws MQClientException {
    this.topic = topic;
    super.subscribe(topic, subExpression);
  }

  @Override
  public void subscribe(String topic, MessageSelector messageSelector) throws MQClientException {
    this.topic = topic;
    super.subscribe(topic, messageSelector);
  }

  @Override
  public void subscribe(String topic, String fullClassName, String filterClassSource) throws MQClientException {
    this.topic = topic;
    super.subscribe(topic, fullClassName, filterClassSource);
  }

  @Override
  public Set<MessageQueue> fetchSubscribeMessageQueues(String topic) throws MQClientException {
    this.topic = topic;
    return super.fetchSubscribeMessageQueues(topic);
  }

  @Override
  public void start() throws MQClientException {
    super.start();
    rebuildSubscriptionData();
  }

  @SuppressWarnings({"deprecation"})
  private void rebuildSubscriptionData() {
    String address = getNamesrvAddr();
    String consumerGroup = getConsumerGroup();
    String consumeScope = RocketMqMessageGrayUtils.buildCacheKey(address, topic, consumerGroup);
    DefaultMQPushConsumerImpl pushConsumerImpl = getDefaultMQPushConsumerImpl();
    RocketMqConsumerImplManager.getInstance().addPushConsumerImpls(consumeScope, pushConsumerImpl);
    ConsumerClientConfig clientConfig = new ConsumerClientConfig(topic, address, consumerGroup,
        pushConsumerImpl.getmQClientFactory());
    ConsumerGroupAutoCheckManager.addConsumerClientConfig(consumeScope, clientConfig);
    ConsumerGroupAutoCheckManager.checkAndStartAutoFindGrayGroup();
    RocketMqSubscriptionDataManager.updateSubscriptionData(pushConsumerImpl.getSubscriptionInner(), topic,
        consumeScope);
  }
}
