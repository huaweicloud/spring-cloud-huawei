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

package com.huaweicloud.rocketmq.grayscale.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.yaml.snakeyaml.DumperOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.representer.Representer;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;
import com.huaweicloud.rocketmq.grayscale.manager.ConsumerGroupAutoCheckManager;
import com.huaweicloud.rocketmq.grayscale.manager.RocketMqConsumerImplManager;

public class MessageGrayPropertiesManager {
  public static final String MESSAGE_GRAY_PREFIX = "rocketmq.gray.config";

  private static final Logger LOGGER = LoggerFactory.getLogger(MessageGrayPropertiesManager.class);

  private final Environment env;

  private final Yaml yaml;

  public MessageGrayPropertiesManager(Environment env) {
    this.env = env;
    GovernanceEventManager.register(this);
    Representer representer = new Representer(new DumperOptions());
    representer.getPropertyUtils().setSkipMissingProperties(true);
    yaml = new Yaml(representer);
    initCacheMessageProperties();
  }

  private void initCacheMessageProperties() {
    String properties = env.getProperty(MESSAGE_GRAY_PREFIX, "");
    if (StringUtils.isEmpty(properties)) {
      return;
    }
    RocketMqMessageGrayProperties messageGrayProperties;
    try {
      messageGrayProperties = yaml.loadAs(properties, RocketMqMessageGrayProperties.class);
    } catch (Exception e) {
      LOGGER.error("Loaded message gray properties failed!", e);
      return;
    }
    if (messageGrayProperties == null) {
      LOGGER.warn("Loaded message gray properties is empty");
      return;
    }
    RocketMqMessageGrayUtils.setMessageGrayProperties(messageGrayProperties);
  }

  @Subscribe
  public void onConfigurationChangedEvent(GovernanceConfigurationChangedEvent event) {
    for (String key : event.getChangedConfigurations()) {
      if (key.startsWith(MESSAGE_GRAY_PREFIX)) {
        initCacheMessageProperties();
        ConsumerGroupAutoCheckManager.checkAndStartAutoFindGrayGroup();
        RocketMqConsumerImplManager.updateConsumerSubscriptionData();
      }
    }
  }
}
