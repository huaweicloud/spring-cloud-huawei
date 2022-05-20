/*

  * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.servicecomb.discovery.ribbon;

import java.util.Date;

import com.netflix.loadbalancer.PollingServerListUpdater;
import org.apache.servicecomb.service.center.client.DiscoveryEvents.InstanceChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.event.EventManager;
import com.netflix.loadbalancer.ServerListUpdater;

public class ServiceCombServerListUpdater implements ServerListUpdater {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombServerListUpdater.class);

  private volatile long lastUpdated = System.currentTimeMillis();

  private UpdateAction updateAction;

  private final PollingServerListUpdater pollingServerListUpdater;

  private static long INITIAL_DELAY_TIME = 5 * 1000;

  private static long REFRESH_INITIAL_TIME = 5 * 1000;

  public ServiceCombServerListUpdater() {
    pollingServerListUpdater = new PollingServerListUpdater(INITIAL_DELAY_TIME, REFRESH_INITIAL_TIME);
    EventManager.getEventBus().register(this);
  }

  @Subscribe
  public void onInstanceChangedEvent(InstanceChangedEvent event) {
    try {
      updateAction.doUpdate();
      lastUpdated = System.currentTimeMillis();
    } catch (Exception e) {
      LOGGER.warn("failed to update serverList", e);
    }
  }

  @Override
  public void start(UpdateAction updateAction) {
    pollingServerListUpdater.start(updateAction);
    this.updateAction = updateAction;
  }

  @Override
  public void stop() {
  }

  @Override
  public String getLastUpdate() {
    return new Date(lastUpdated).toString();
  }

  @Override
  public long getDurationSinceLastUpdateMs() {
    return System.currentTimeMillis() - lastUpdated;
  }

  @Override
  public int getNumberMissedCycles() {
    return 0;
  }

  @Override
  public int getCoreThreads() {
    return 0;
  }
}