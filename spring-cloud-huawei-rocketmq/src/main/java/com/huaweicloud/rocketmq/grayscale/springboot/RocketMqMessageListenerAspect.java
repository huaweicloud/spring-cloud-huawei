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

import org.apache.rocketmq.common.message.Message;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;

import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;

@Aspect
public class RocketMqMessageListenerAspect {
  @Pointcut("execution(* org.apache.rocketmq.spring.core.RocketMQListener.*(..))")
  public void pointcut() {
  }

  @Before("pointcut()")
  public void beforeOnMessage(JoinPoint joinPoint) {
    Object message = joinPoint.getArgs()[0];
    if (message == null) {
      return;
    }
    if (message instanceof Message) {
      RocketMqMessageGrayUtils.setInvocationContext(((Message) message).getProperties());
    }
  }
}
