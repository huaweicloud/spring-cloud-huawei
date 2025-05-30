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

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.env.Environment;

import com.huaweicloud.rocketmq.grayscale.config.MessageGrayPropertiesManager;
import com.huaweicloud.rocketmq.grayscale.servicemeta.ServiceMetaManager;
import com.huaweicloud.rocketmq.grayscale.springboot.RocketMqMessageListenerAspect;
import com.huaweicloud.rocketmq.grayscale.springboot.RocketMqListenerContainerAspect;
import com.huaweicloud.rocketmq.grayscale.filter.WebMvcGrayHeaderFilter;
import com.huaweicloud.rocketmq.grayscale.servicemeta.NacosServiceMeta;
import com.huaweicloud.rocketmq.grayscale.servicemeta.ServicecombServiceMeta;
import com.huaweicloud.rocketmq.grayscale.springboot.RocketMqSendMessageHookManager;

@Configuration
@ConditionalOnRocketMqMsgGrayEnabled
@AutoConfigureAfter(MessageGrayPropertiesManager.class)
public class RocketMqMessageGrayConfiguration {
  @Bean
  @ConfigurationProperties("spring.cloud.nacos.discovery")
  public NacosServiceMeta serviceMetaInfo() {
    return new NacosServiceMeta();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.instance")
  public ServicecombServiceMeta servicecombServiceMeta() {
    return new ServicecombServiceMeta();
  }

  @Bean
  public MessageGrayPropertiesManager messageGrayPropertiesManager(Environment env) {
    return new MessageGrayPropertiesManager(env);
  }

  @Bean
  public ServiceMetaManager serviceMetaManager(NacosServiceMeta serviceMeta, ServicecombServiceMeta servicecombServiceMeta) {
    return new ServiceMetaManager(serviceMeta, servicecombServiceMeta);
  }

  @Bean
  @ConditionalOnClass(name = "org.apache.rocketmq.spring.support.DefaultRocketMQListenerContainer")
  @DependsOn(value = {"messageGrayPropertiesManager", "serviceMetaManager"})
  public RocketMqListenerContainerAspect rocketMQListenerContainerAspect() {
    return new RocketMqListenerContainerAspect();
  }

  @Bean
  @ConditionalOnClass(name = "org.apache.rocketmq.spring.core.RocketMQListener")
  public RocketMqMessageListenerAspect RocketMqListenerAspect() {
    return new RocketMqMessageListenerAspect();
  }

  @Bean
  public RocketMqSendMessageHookManager rocketMqMessageHookManager(ApplicationContext applicationContext) {
    return new RocketMqSendMessageHookManager(applicationContext);
  }

  @Bean
  public WebMvcGrayHeaderFilter webMvcGrayHeaderFilter() {
    return new WebMvcGrayHeaderFilter();
  }
}
