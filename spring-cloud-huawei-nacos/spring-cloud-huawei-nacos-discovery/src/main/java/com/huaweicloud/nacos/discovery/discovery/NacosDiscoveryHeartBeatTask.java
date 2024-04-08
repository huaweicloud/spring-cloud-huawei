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

package com.huaweicloud.nacos.discovery.discovery;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NacosDiscoveryHeartBeatTask implements ApplicationEventPublisherAware, SmartLifecycle {
  private final NacosDiscoveryProperties properties;

  private final ThreadPoolTaskScheduler taskScheduler;

  private ApplicationEventPublisher eventPublisher;

  private final AtomicBoolean running = new AtomicBoolean(false);

  private final AtomicLong nacosHeartBeatTimes = new AtomicLong(0);

  public NacosDiscoveryHeartBeatTask(NacosDiscoveryProperties properties) {
    this.properties = properties;
    this.taskScheduler = buildTaskScheduler();
  }

  private ThreadPoolTaskScheduler buildTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setBeanName("Nacos-HeartBeat-Task-Scheduler");
    taskScheduler.initialize();
    return taskScheduler;
  }


  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher eventPublisher) {
    this.eventPublisher = eventPublisher;
  }

  @Override
  public void start() {
    if (this.running.compareAndSet(false, true)) {
      taskScheduler.scheduleWithFixedDelay(
          this::publishDiscoveryHeartBeat, Duration.ofMillis(properties.getHeartBeatTaskDelay()));
    }
  }

  private void publishDiscoveryHeartBeat() {
    HeartbeatEvent event = new HeartbeatEvent(this, nacosHeartBeatTimes.getAndIncrement());
    this.eventPublisher.publishEvent(event);
  }

  @Override
  public void stop() {
    if (this.running.compareAndSet(true, false)) {
      this.taskScheduler.shutdown();
    }
  }

  @Override
  public boolean isRunning() {
    return this.running.get();
  }
}
