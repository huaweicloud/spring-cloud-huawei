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

public class GovernanceData {
  private String name;

  public String getName() {
    return name;
  }

  public GovernanceData setName(String name) {
    this.name = name;
    return this;
  }

  private long successfulCalls;

  private long failedCalls;

  private long ignoredCalls;

  private double totalTime;

  private double failureRate;

  private double slowRate;

  private boolean circuitBreakerOpen;

  private long shortCircuitedCalls;

  private long timeInMillis;

  public long getSuccessfulCalls() {
    return successfulCalls;
  }

  public GovernanceData setSuccessfulCalls(long successfulCalls) {
    this.successfulCalls = successfulCalls;
    return this;
  }

  public long getFailedCalls() {
    return failedCalls;
  }

  public GovernanceData setFailedCalls(long failedCalls) {
    this.failedCalls = failedCalls;
    return this;
  }

  public long getIgnoredCalls() {
    return ignoredCalls;
  }

  public GovernanceData setIgnoredCalls(long ignoredCalls) {
    this.ignoredCalls = ignoredCalls;
    return this;
  }

  public double getTotalTime() {
    return totalTime;
  }

  public GovernanceData setTotalTime(double totalTime) {
    this.totalTime = totalTime;
    return this;
  }

  public double getFailureRate() {
    return failureRate;
  }

  public GovernanceData setFailureRate(double failureRate) {
    this.failureRate = failureRate;
    return this;
  }

  public double getSlowRate() {
    return slowRate;
  }

  public GovernanceData setSlowRate(double slowRate) {
    this.slowRate = slowRate;
    return this;
  }

  public boolean isCircuitBreakerOpen() {
    return circuitBreakerOpen;
  }

  public GovernanceData setCircuitBreakerOpen(boolean circuitBreakerOpen) {
    this.circuitBreakerOpen = circuitBreakerOpen;
    return this;
  }

  public long getShortCircuitedCalls() {
    return shortCircuitedCalls;
  }

  public GovernanceData setShortCircuitedCalls(long shortCircuitedCalls) {
    this.shortCircuitedCalls = shortCircuitedCalls;
    return this;
  }

  public long getTimeInMillis() {
    return timeInMillis;
  }

  public void setTimeInMillis(long timeInMillis) {
    this.timeInMillis = timeInMillis;
  }
}
