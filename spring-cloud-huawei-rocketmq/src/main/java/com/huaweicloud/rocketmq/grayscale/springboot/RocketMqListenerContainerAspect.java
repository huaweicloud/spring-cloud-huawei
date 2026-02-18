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

package com.huaweicloud.rocketmq.grayscale.springboot;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.impl.consumer.DefaultMQPushConsumerImpl;
import org.apache.rocketmq.client.impl.factory.MQClientInstance;
import org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import com.huaweicloud.rocketmq.grayscale.manager.ConsumerGroupAutoCheckManager;
import com.huaweicloud.rocketmq.grayscale.manager.RocketMqConsumerImplManager;
import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;
import com.huaweicloud.rocketmq.grayscale.manager.RocketMqSubscriptionDataManager;
import com.huaweicloud.rocketmq.grayscale.config.ConsumerClientConfig;

@Aspect
public class RocketMqListenerContainerAspect {
  @Pointcut("execution(* org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer.start())")
  public void pointcut() {
  }

  @Before("pointcut()")
  public void beforeStart(JoinPoint joinPoint) {
    DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) joinPoint.getTarget();
    String consumerGroup = container.getConsumerGroup();
    String grayConsumerGroup = RocketMqMessageGrayUtils.getGrayConsumerGroup(consumerGroup);
    if (StringUtils.equals(consumerGroup, grayConsumerGroup)) {
      return;
    }
    container.setConsumerGroup(grayConsumerGroup);
    container.getConsumer().setConsumerGroup(grayConsumerGroup);
  }

  @After("pointcut()")
  public void afterStart(JoinPoint joinPoint) {
    DefaultRocketMQListenerContainer container = (DefaultRocketMQListenerContainer) joinPoint.getTarget();
    String consumeScope = RocketMqMessageGrayUtils.buildCacheKey(container.getConsumer().getNamesrvAddr(),
        container.getTopic(), container.getConsumer().getConsumerGroup());
    DefaultMQPushConsumerImpl pushConsumerImpl = getPushConsumerImpl(container);
    RocketMqConsumerImplManager.getInstance().addPushConsumerImpls(consumeScope, pushConsumerImpl);
    addConsumerClientConfig(container.getConsumer().getNamesrvAddr(), container.getTopic(),
        container.getConsumer().getConsumerGroup(), pushConsumerImpl.getmQClientFactory());
    ConsumerGroupAutoCheckManager.checkAndStartAutoFindGrayGroup();
    RocketMqSubscriptionDataManager.updateSubscriptionData(pushConsumerImpl.getSubscriptionInner(),
        container.getTopic(), consumeScope);
  }

  private void addConsumerClientConfig(String namesrvAddr, String topic, String consumerGroup,
      MQClientInstance mqClientInstance) {
    ConsumerClientConfig clientConfig = new ConsumerClientConfig(topic, namesrvAddr, consumerGroup, mqClientInstance);
    String consumeScope = RocketMqMessageGrayUtils.buildCacheKey(namesrvAddr, topic, consumerGroup);
    ConsumerGroupAutoCheckManager.addConsumerClientConfig(consumeScope, clientConfig);
  }

  @SuppressWarnings({"deprecation"})
  private DefaultMQPushConsumerImpl getPushConsumerImpl(DefaultRocketMQListenerContainer container) {
    return container.getConsumer().getDefaultMQPushConsumerImpl();
  }
}
