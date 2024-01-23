/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

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


package com.huaweicloud.servicecomb.dashboard;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.service.center.client.RegistrationEvents.MicroserviceInstanceRegistrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.service.engine.common.configration.dynamic.DashboardProperties;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.servicecomb.dashboard.model.MonitorDataProvider;
import com.huaweicloud.servicecomb.dashboard.model.MonitorDataPublisher;

import io.netty.util.concurrent.DefaultThreadFactory;

public class DataFactory {
  private static final Logger LOGGER = LoggerFactory.getLogger(DataFactory.class);

  private static final int CORE_SIZE = 1;

  private volatile boolean hasStart = false;

  private volatile boolean sendingData = false;

  private final List<MonitorDataProvider> dataProviders;

  private final MonitorDataPublisher monitorDataPublisher;

  private final ScheduledExecutorService executorService;

  private final DashboardProperties dashboardProperties;

  public DataFactory(List<MonitorDataProvider> dataProviders, MonitorDataPublisher monitorDataPublisher,
      DashboardProperties dashboardProperties) {
    ThreadFactory threadFactory = new DefaultThreadFactory("dashboard", true);
    executorService = Executors.newScheduledThreadPool(CORE_SIZE, threadFactory);
    this.dataProviders = dataProviders;
    this.monitorDataPublisher = monitorDataPublisher;
    this.dashboardProperties = dashboardProperties;
    EventManager.register(this);
  }

  @Subscribe
  public void onMicroserviceInstanceRegistrationEvent(MicroserviceInstanceRegistrationEvent event) {
    if (event.isSuccess()) {
      start();
    }
  }

  private void start() {
    if (!hasStart) {
      hasStart = true;

      monitorDataPublisher.init();

      StringBuilder sb = new StringBuilder();
      sb.append("Monitor data sender started. Configured data providers is {");
      for (MonitorDataProvider provider : dataProviders) {
        sb.append(provider.getClass().getName());
        sb.append(",");
      }
      sb.append("}");
      LOGGER.info(sb.toString());

      executorService.scheduleWithFixedDelay(() -> {
        try {
          if (sendingData) {
            return;
          }
          sendingData = true;
          sendData();
        } catch (Throwable e) {
          LOGGER.error("send monitor data error.", e);
        } finally {
          sendingData = false;
        }
      }, dashboardProperties.getIntervalInMills(), dashboardProperties.getIntervalInMills(), TimeUnit.MILLISECONDS);
    }
  }

  private void sendData() {
    for (MonitorDataProvider provider : this.dataProviders) {
      if (provider.enabled()) {
        this.monitorDataPublisher.publish(provider);
      }
    }
  }
}
