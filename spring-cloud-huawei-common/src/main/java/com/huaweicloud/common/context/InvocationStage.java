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

package com.huaweicloud.common.context;

import java.util.HashMap;
import java.util.Map;

import com.huaweicloud.common.event.EventManager;

/**
 * Recording time for invocation stage.
 */
public class InvocationStage {
  public static final String STAGE_ALL = "all";

  public static final String STAGE_FEIGN = "feign";

  public static final String STAGE_WEB_CLIENT = "webClient";

  public static final String STAGE_GATEWAY = "gateway";

  public static final String STAGE_REST_TEMPLATE = "restTemplate";

  public static class Stage {
    private long beginTime;

    private long endTime;

    public long getBeginTime() {
      return beginTime;
    }

    public void setBeginTime(long beginTime) {
      this.beginTime = beginTime;
    }

    public long getEndTime() {
      return endTime;
    }

    public void setEndTime(long endTime) {
      this.endTime = endTime;
    }
  }

  private final InvocationContext invocationContext;

  // invocation stage can not be used in concurrent access
  private final Map<String, Stage> stages = new HashMap<>();

  private int statusCode;

  /**
   * 唯一标记一个方法。可以采用 Provider 的方法， 或者从在请求链中，使用入口请求的方法粒度来统计。
   */
  private String id;

  private long beginTime;

  private long endTime;

  public InvocationStage(InvocationContext invocationContext) {
    this.invocationContext = invocationContext;
  }

  /*
   * Holder for testing
   */
  protected long nanoTime() {
    return System.nanoTime();
  }

  public String recordStageBegin(String stageName) {
    String realStageName = stageName;
    if (stages.get(stageName) != null) {
      realStageName = realStageName + "@";
    }
    Stage stage = new Stage();
    stage.beginTime = System.nanoTime();
    stages.put(realStageName, stage);
    return realStageName;
  }

  public void recordStageEnd(String stageName) {
    Stage stage = stages.get(stageName);
    stage.endTime = nanoTime();
  }

  public void begin(String id) {
    this.id = id;
    this.beginTime = nanoTime();
  }

  public void finish(int statusCode) {
    this.statusCode = statusCode;
    this.endTime = nanoTime();
    InvocationFinishEvent event = new InvocationFinishEvent(this);
    EventManager.getEventBoundedAsyncEventBus().post(event);
    EventManager.getEventBus().post(event);
  }

  public InvocationContext getInvocationContext() {
    return this.invocationContext;
  }

  public int getStatusCode() {
    return this.statusCode;
  }

  public String getId() {
    return this.id;
  }

  public long getBeginTime() {
    return this.beginTime;
  }

  public long getEndTime() {
    return this.endTime;
  }

  public Map<String, Stage> getStages() {
    return this.stages;
  }
}
