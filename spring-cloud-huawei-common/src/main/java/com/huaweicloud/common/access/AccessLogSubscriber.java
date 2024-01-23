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

package com.huaweicloud.common.access;

import java.util.concurrent.TimeUnit;

import com.google.common.eventbus.Subscribe;
import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationFinishEvent;
import com.huaweicloud.common.event.EventManager;

/**
 * Listen invocation events and write access logs
 */
public class AccessLogSubscriber {
  private final ContextProperties contextProperties;

  private final AccessLogLogger accessLogLogger;

  public AccessLogSubscriber(ContextProperties contextProperties, AccessLogLogger accessLogLogger) {
    this.contextProperties = contextProperties;
    this.accessLogLogger = accessLogLogger;
    if (contextProperties.isEnableAsyncTrace()) {
      EventManager.getEventBoundedAsyncEventBus().register(this);
    } else {
      EventManager.getEventBus().register(this);
    }
  }

  @Subscribe
  public void onInvocationFinishEvent(InvocationFinishEvent event) {
    if (!contextProperties.isEnableTraceInfo()) {
      return;
    }

    accessLogLogger.log(event.getInvocationStage().getInvocationContext(),
        event.getInvocationStage().getId(),
        event.getInvocationStage().getStatusCode(),
        TimeUnit.NANOSECONDS.toMillis(event.getInvocationStage().getEndTime()
            - event.getInvocationStage().getBeginTime()));
  }
}
