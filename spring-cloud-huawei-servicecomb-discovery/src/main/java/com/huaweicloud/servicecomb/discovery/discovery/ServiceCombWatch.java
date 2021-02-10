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
package com.huaweicloud.servicecomb.discovery.discovery;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.context.SmartLifecycle;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * @Author GuoYl123
 * @Date 2020/8/25
 **/
public class ServiceCombWatch implements ApplicationEventPublisherAware, SmartLifecycle {

  private ServiceCombDiscoveryProperties discoveryProperties;

  private final AtomicBoolean isActive = new AtomicBoolean(false);

  private ApplicationEventPublisher publisher;

  private ScheduledFuture<?> watchFuture;

  private final TaskScheduler taskScheduler = new ConcurrentTaskScheduler(
      Executors.newSingleThreadScheduledExecutor());

  private final AtomicLong index = new AtomicLong(0);

  public ServiceCombWatch(
      ServiceCombDiscoveryProperties discoveryProperties) {
    this.discoveryProperties = discoveryProperties;
  }

  @Override
  public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
    this.publisher = applicationEventPublisher;
  }

  @Override
  public void start() {
    if (this.isActive.compareAndSet(false, true)) {
      this.watchFuture = this.taskScheduler.scheduleWithFixedDelay(
          () -> this.publisher.publishEvent(new HeartbeatEvent(this, index.getAndIncrement())),
          discoveryProperties.getRefreshInterval());
    }
  }

  @Override
  public void stop() {
    if (this.isActive.compareAndSet(true, false) && this.watchFuture != null) {
      this.watchFuture.cancel(true);
    }
  }

  @Override
  public boolean isRunning() {
    return this.isActive.get();
  }

  @Override
  public boolean isAutoStartup() {
    return true;
  }

  @Override
  public void stop(Runnable runnable) {
    stop();
    runnable.run();
  }

  @Override
  public int getPhase() {
    return Integer.MAX_VALUE;
  }
}
