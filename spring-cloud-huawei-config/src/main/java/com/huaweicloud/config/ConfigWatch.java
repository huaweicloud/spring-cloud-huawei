/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.config;

import java.util.HashSet;
import java.util.Set;

import org.apache.servicecomb.config.common.ConfigurationChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.event.ConfigRefreshEvent;
import com.huaweicloud.common.event.EventManager;

public class ConfigWatch implements ApplicationEventPublisherAware {

  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigWatch.class);

  private ApplicationEventPublisher applicationEventPublisher;

  public ConfigWatch() {
    EventManager.register(this);
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.applicationEventPublisher = applicationEventPublisher;
  }

  @Subscribe
  public void onConfigurationChangedEvent(ConfigurationChangedEvent event) {
    LOGGER.info("receive new configurations, added=[{}], updated=[{}], deleted=[{}]",
        event.getAdded().keySet(),
        event.getUpdated().keySet(),
        event.getDeleted().keySet());

    Set<String> updatedKey = new HashSet<>();
    updatedKey.addAll(event.getAdded().keySet());
    updatedKey.addAll(event.getUpdated().keySet());
    updatedKey.addAll(event.getDeleted().keySet());
    ConfigRefreshEvent configRefreshEvent = new ConfigRefreshEvent(this, updatedKey);
    applicationEventPublisher.publishEvent(configRefreshEvent);
    applicationEventPublisher.publishEvent(new RefreshEvent(this, configRefreshEvent, "Config refreshed"));
  }
}
