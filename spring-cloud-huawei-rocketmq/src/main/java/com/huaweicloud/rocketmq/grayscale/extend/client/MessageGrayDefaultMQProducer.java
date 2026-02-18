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

import java.util.List;

import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.MixAll;
import org.apache.rocketmq.remoting.RPCHook;

import com.huaweicloud.rocketmq.grayscale.holder.RocketMqGraySendMessageHook;

public class MessageGrayDefaultMQProducer extends DefaultMQProducer {
  public MessageGrayDefaultMQProducer() {
    this(MixAll.DEFAULT_PRODUCER_GROUP);
  }

  public MessageGrayDefaultMQProducer(RPCHook rpcHook) {
    this(MixAll.DEFAULT_PRODUCER_GROUP, rpcHook);
  }

  public MessageGrayDefaultMQProducer(final String producerGroup) {
    this(producerGroup, (RPCHook) null);
  }

  public MessageGrayDefaultMQProducer(final String producerGroup, RPCHook rpcHook) {
    this(producerGroup, rpcHook, null);
  }

  public MessageGrayDefaultMQProducer(final String producerGroup, RPCHook rpcHook,
      final List<String> topics) {
    this(producerGroup, rpcHook, topics, false, null);
  }

  public MessageGrayDefaultMQProducer(final String producerGroup, boolean enableMsgTrace, final String customizedTraceTopic) {
    this(producerGroup, null, enableMsgTrace, customizedTraceTopic);
  }

  public MessageGrayDefaultMQProducer(final String producerGroup, RPCHook rpcHook, boolean enableMsgTrace,
      final String customizedTraceTopic) {
    this(producerGroup, rpcHook, null, enableMsgTrace, customizedTraceTopic);
  }

  public MessageGrayDefaultMQProducer(final String producerGroup, RPCHook rpcHook, final List<String> topics,
      boolean enableMsgTrace, final String customizedTraceTopic) {
    super(producerGroup, rpcHook, topics, enableMsgTrace, customizedTraceTopic);
    setMessageSendHook();
  }

  @SuppressWarnings({"deprecation"})
  private void setMessageSendHook() {
    getDefaultMQProducerImpl().registerSendMessageHook(new RocketMqGraySendMessageHook());
  }
}
