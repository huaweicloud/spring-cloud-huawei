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

package com.huaweicloud.common.transport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author GuoYl123
 * @Date 2020/4/18
 **/
public class BackOff {

  private static final Logger LOGGER = LoggerFactory.getLogger(BackOff.class);

  private static final int MAX_DELAY_TIME = 60 * 1000;

  private int retryDelayTime = 1000;

  public BackOff() {
  }

  public BackOff(int retryDelayTime) {
    this.retryDelayTime = retryDelayTime;
  }

  public void backOff() {
    if (MAX_DELAY_TIME == retryDelayTime) {
      return;
    }
    retryDelayTime *= 2;
    if (MAX_DELAY_TIME <= retryDelayTime) {
      retryDelayTime = MAX_DELAY_TIME;
    }
  }

  public void waiting() {
    try {
      Thread.sleep(getBackOffTime());
    } catch (InterruptedException e) {
      LOGGER.warn("thread interrupted.");
    }
  }

  public void waitingAndBackoff() {
    try {
      Thread.sleep(getBackOffTime());
    } catch (InterruptedException e) {
      LOGGER.warn("thread interrupted.");
    }
    backOff();
  }

  private int getBackOffTime() {
    return retryDelayTime;
  }
}
