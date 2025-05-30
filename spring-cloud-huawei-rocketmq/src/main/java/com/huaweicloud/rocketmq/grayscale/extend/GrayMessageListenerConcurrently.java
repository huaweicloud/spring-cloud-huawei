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

package com.huaweicloud.rocketmq.grayscale.extend;

import java.util.List;

import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeConcurrentlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerConcurrently;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;

public interface GrayMessageListenerConcurrently extends MessageListenerConcurrently {
  /**
   * origin rocketmq client listen message method, if microservice calls need routing by message properties,
   * this method cannot be implemented. Instead, the 'consumeMessageExtend' method needs to be implemented.
   *
   * @param messages messages
   * @param context current consumer context
   * @return ConsumeConcurrentlyStatus
   */
  @Override
  default ConsumeConcurrentlyStatus consumeMessage(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
    if (!CollectionUtils.isEmpty(messages)) {
      RocketMqMessageGrayUtils.setInvocationContext(messages.get(0).getProperties());
    }
    return this.consumeMessageExtend(messages, context);
  }

  default ConsumeConcurrentlyStatus consumeMessageExtend(List<MessageExt> messages, ConsumeConcurrentlyContext context) {
    return ConsumeConcurrentlyStatus.CONSUME_SUCCESS;
  };
}
