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

package com.huaweicloud.rocketmq.grayscale.holder;

import java.util.Map;

import org.apache.rocketmq.client.hook.SendMessageContext;
import org.apache.rocketmq.client.hook.SendMessageHook;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;

public class RocketMqGraySendMessageHook implements SendMessageHook {
  private static final Logger LOGGER = LoggerFactory.getLogger(RocketMqGraySendMessageHook.class);

  @Override
  public String hookName() {
    return "MessageGraySendMessageHook";
  }

  @Override
  public void sendMessageBefore(SendMessageContext context) {
    Message message = context.getMessage();

    // set traffic tags in message by matching serviceMeta
    if (injectTrafficTagByServiceMeta(message)) {
      return;
    }

    // set traffic tags in message by matching trafficTags
    injectTrafficTagByTrafficTag(message);
  }

  private void injectTrafficTagByTrafficTag(Message message) {
    Map<String, String> trafficGrayHeaders = RequestGrayHeaderHolder.getRequestGrayHeader();
    if (CollectionUtils.isEmpty(trafficGrayHeaders)) {
      return;
    }
    Map<String, String> grayTags = RocketMqMessageGrayUtils.getGrayTagsByGrayHeaders(trafficGrayHeaders);
    if (grayTags.isEmpty()) {
      return;
    }
    for (Map.Entry<String, String> entry : grayTags.entrySet()) {
      message.putUserProperty(entry.getKey(), entry.getValue());
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("set property for message by gray header, messageId: " + message.getProperty("UNIQ_KEY") +
            ", traffic tag: " + entry.getValue());
      }
    }
  }

  private boolean injectTrafficTagByServiceMeta(Message message) {
    Map<String, String> grayTags = RocketMqMessageGrayUtils.getGrayTagsByServiceMeta();
    if (grayTags.isEmpty()) {
      return false;
    }
    for (Map.Entry<String, String> entry : grayTags.entrySet()) {
      if (message.getProperties() == null || !message.getProperties().containsKey(entry.getKey())) {
        message.putUserProperty(entry.getKey(), entry.getValue());
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("set property for message by service meta, messageId: " + message.getProperty("UNIQ_KEY") + ", "
              + "traffic tag: " + entry.getValue());
        }
      }
    }
    return true;
  }

  @Override
  public void sendMessageAfter(SendMessageContext context) {
  }
}
