/*

 * Copyright (C) 2020-2026 Huawei Technologies Co., Ltd. All rights reserved.

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

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.cloud.client.discovery.event.HeartbeatEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NacosDiscoveryHeartBeatTaskTest {

  private NacosDiscoveryProperties properties;

  private ApplicationEventPublisher eventPublisher;

  private NacosDiscoveryHeartBeatTask heartBeatTask;

  @BeforeEach
  public void setUp() {
    properties = new NacosDiscoveryProperties();
    properties.setHeartBeatTaskDelay(50);
    eventPublisher = Mockito.mock(ApplicationEventPublisher.class);
    heartBeatTask = new NacosDiscoveryHeartBeatTask(properties);
    heartBeatTask.setApplicationEventPublisher(eventPublisher);
  }

  @AfterEach
  public void tearDown() {
    heartBeatTask.stop();
  }

  // Test scenario: isRunning() returns false before start() is called
  @Test
  public void isRunningReturnsFalseBeforeStart() {
    Assertions.assertFalse(heartBeatTask.isRunning());
  }

  // Test scenario: start() sets running state to true
  @Test
  public void startSetsRunningToTrue() {
    heartBeatTask.start();

    Assertions.assertTrue(heartBeatTask.isRunning());
  }

  // Test scenario: start() schedules task and HeartbeatEvent is published with correct source
  @Test
  public void startPublishesHeartbeatEvent() throws InterruptedException {
    heartBeatTask.start();
    Thread.sleep(300);

    ArgumentCaptor<HeartbeatEvent> captor = ArgumentCaptor.forClass(HeartbeatEvent.class);
    Mockito.verify(eventPublisher, Mockito.atLeastOnce()).publishEvent(captor.capture());

    HeartbeatEvent event = captor.getValue();
    Assertions.assertNotNull(event);
    Assertions.assertSame(heartBeatTask, event.getSource());

    // Verify multiple heartbeat events were published (periodic scheduling)
    Assertions.assertTrue(captor.getAllValues().size() >= 2);
  }

  // Test scenario: calling start() twice does not reschedule the task
  @Test
  public void startCalledTwiceDoesNotReschedule() {
    heartBeatTask.start();
    heartBeatTask.start();

    Assertions.assertTrue(heartBeatTask.isRunning());
  }

  // Test scenario: stop() sets running state to false after start
  @Test
  public void stopSetsRunningToFalse() {
    heartBeatTask.start();
    heartBeatTask.stop();

    Assertions.assertFalse(heartBeatTask.isRunning());
  }

  // Test scenario: stop() without start() is a safe no-op
  @Test
  public void stopWithoutStartIsSafeNoOp() {
    heartBeatTask.stop();

    Assertions.assertFalse(heartBeatTask.isRunning());
  }
}