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
package com.huaweicloud.governance.policy;

/**
 *
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class RateLimitingPolicy extends AbstractPolicy {

  private Integer timeoutDuration;

  private Integer limitRefreshPeriod;

  private Integer limitForPeriod;

  // 简化配置
  private Integer rate;

  public Integer getTimeoutDuration() {
    return timeoutDuration;
  }

  public void setTimeoutDuration(Integer timeoutDuration) {
    this.timeoutDuration = timeoutDuration;
  }

  public Integer getLimitRefreshPeriod() {
    return limitRefreshPeriod;
  }

  public void setLimitRefreshPeriod(Integer limitRefreshPeriod) {
    this.limitRefreshPeriod = limitRefreshPeriod;
  }

  public Integer getLimitForPeriod() {
    return limitForPeriod;
  }

  public void setLimitForPeriod(Integer limitForPeriod) {
    this.limitForPeriod = limitForPeriod;
  }

  public Integer getRate() {
    return rate;
  }

  public void setRate(Integer rate) {
    this.rate = rate;
  }

  public RateLimitingPolicy() {
  }

  @Override
  public String handler() {
    return "GovRateLimiting";
  }

  @Override
  public boolean legal() {
    return (timeoutDuration != null && limitRefreshPeriod != null && limitForPeriod != null) || rate != null;
  }

  @Override
  public boolean simple() {
    return !(timeoutDuration != null && limitRefreshPeriod != null && limitForPeriod != null) && rate != null;
  }
}
