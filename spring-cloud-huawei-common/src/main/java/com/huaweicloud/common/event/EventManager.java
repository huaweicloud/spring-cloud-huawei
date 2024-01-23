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

package com.huaweicloud.common.event;

import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;

import io.netty.util.concurrent.DefaultThreadFactory;

public class EventManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventManager.class);

  private static final EventBus eventBus = new EventBus();

  private static final int ASYNC_QUEUE_SIZE = 100000;

  private volatile static EventBus eventBoundedAsyncEventBus;

  private static final Object LOCK = new Object();

  public static EventBus getEventBus() {
    return eventBus;
  }

  /**
   * An async event bus that event may be discarded when too many events are queued
   */
  public static EventBus getEventBoundedAsyncEventBus() {
    if (eventBoundedAsyncEventBus == null) {
      synchronized (LOCK) {
        if (eventBoundedAsyncEventBus == null) {
          Executor executor = new ThreadPoolExecutor(2, 20,
              60000L, TimeUnit.MILLISECONDS,
              new LinkedBlockingQueue<>(ASYNC_QUEUE_SIZE),
              new DefaultThreadFactory("event", true),
              (r, e) -> LOGGER.warn("Event discarded"));
          eventBoundedAsyncEventBus = new AsyncEventBus("async", executor);
        }
      }
    }
    return eventBoundedAsyncEventBus;
  }

  public static void post(Object event) {
    eventBus.post(event);
  }

  public static void register(Object subscriber) {
    eventBus.register(subscriber);
  }
}
