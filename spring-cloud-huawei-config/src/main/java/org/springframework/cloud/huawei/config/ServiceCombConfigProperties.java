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

package org.springframework.cloud.huawei.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @Author wangqijun
 * @Date 11:05 2019-10-17
 **/
@Component
@ConfigurationProperties("spring.cloud.servicecomb.config")
public class ServiceCombConfigProperties {

  private boolean enable = true;

  private String serverAddr;

  private Watch watch;

  public boolean isEnable() {
    return enable;
  }

  public void setEnable(boolean enable) {
    this.enable = enable;
  }

  public String getServerAddr() {
    return serverAddr;
  }

  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
  }

  public Watch getWatch() {
    return watch;
  }

  public void setWatch(Watch watch) {
    this.watch = watch;
  }

  public static class Watch {
    private boolean enable;

    private int delay = 10 * 1000;

    private int waitTime;

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public int getDelay() {
      return delay;
    }

    public void setDelay(int delay) {
      this.delay = delay;
    }

    public int getWaitTime() {
      return waitTime;
    }

    public void setWaitTime(int waitTime) {
      this.waitTime = waitTime;
    }
  }
}
