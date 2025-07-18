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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.remoting.protocol.heartbeat.SubscriptionData;

public class RocketMqConsumerImplManager {
  private final static RocketMqConsumerImplManager instance = new RocketMqConsumerImplManager();

  private final static Map<String, DefaultMQPushConsumerImpl> pushConsumerImpls = new ConcurrentHashMap<>();

  private RocketMqConsumerImplManager() {
  }

  public static RocketMqConsumerImplManager getInstance() {
    return instance;
  }

  public void addPushConsumerImpls(String consumeScope, DefaultMQPushConsumerImpl pushConsumer) {
    pushConsumerImpls.putIfAbsent(consumeScope, pushConsumer);
  }

  public Map<String, SubscriptionData> getSubscriptionInner(String consumeScope) {
    if (pushConsumerImpls.get(consumeScope) != null) {
      return pushConsumerImpls.get(consumeScope).getSubscriptionInner();
    }
    return new HashMap<>();
  }

  public static void updateConsumerSubscriptionData() {
    if (!pushConsumerImpls.isEmpty()) {
      for (Map.Entry<String, DefaultMQPushConsumerImpl> consumerImplEntry : pushConsumerImpls.entrySet()) {
        String consumeScope = consumerImplEntry.getKey();
        ConcurrentMap<String, SubscriptionData> subscriptionInner = consumerImplEntry.getValue().getSubscriptionInner();
        RocketMqSubscriptionDataManager.updateSubscriptionData(subscriptionInner,
            getTopicFromConsumeScope(consumeScope), consumeScope);
      }
    }
  }

  private static String getTopicFromConsumeScope(String consumeScope) {
    return consumeScope.split("@")[1];
  }
}
