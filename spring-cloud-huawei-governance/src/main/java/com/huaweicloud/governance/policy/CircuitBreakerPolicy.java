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
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class CircuitBreakerPolicy extends AbstractPolicy {

  private Integer failureRateThreshold;

  private Integer slowCallRateThreshold;

  private Integer waitDurationInOpenState;

  private Integer slowCallDurationThreshold;

  private Integer permittedNumberOfCallsInHalfOpenState;

  private Integer minimumNumberOfCalls;

  private Integer slidingWindowType;

  private Integer slidingWindowSize;

  public CircuitBreakerPolicy() {
  }

  public Integer getFailureRateThreshold() {
    return failureRateThreshold;
  }

  public void setFailureRateThreshold(Integer failureRateThreshold) {
    this.failureRateThreshold = failureRateThreshold;
  }

  public Integer getSlowCallRateThreshold() {
    return slowCallRateThreshold;
  }

  public void setSlowCallRateThreshold(Integer slowCallRateThreshold) {
    this.slowCallRateThreshold = slowCallRateThreshold;
  }

  public Integer getWaitDurationInOpenState() {
    return waitDurationInOpenState;
  }

  public void setWaitDurationInOpenState(Integer waitDurationInOpenState) {
    this.waitDurationInOpenState = waitDurationInOpenState;
  }

  public Integer getSlowCallDurationThreshold() {
    return slowCallDurationThreshold;
  }

  public void setSlowCallDurationThreshold(Integer slowCallDurationThreshold) {
    this.slowCallDurationThreshold = slowCallDurationThreshold;
  }

  public Integer getPermittedNumberOfCallsInHalfOpenState() {
    return permittedNumberOfCallsInHalfOpenState;
  }

  public void setPermittedNumberOfCallsInHalfOpenState(Integer permittedNumberOfCallsInHalfOpenState) {
    this.permittedNumberOfCallsInHalfOpenState = permittedNumberOfCallsInHalfOpenState;
  }

  public Integer getMinimumNumberOfCalls() {
    return minimumNumberOfCalls;
  }

  public void setMinimumNumberOfCalls(Integer minimumNumberOfCalls) {
    this.minimumNumberOfCalls = minimumNumberOfCalls;
  }

  public Integer getSlidingWindowType() {
    return slidingWindowType;
  }

  public void setSlidingWindowType(Integer slidingWindowType) {
    this.slidingWindowType = slidingWindowType;
  }

  public Integer getSlidingWindowSize() {
    return slidingWindowSize;
  }

  public void setSlidingWindowSize(Integer slidingWindowSize) {
    this.slidingWindowSize = slidingWindowSize;
  }

  @Override
  public String handler() {
    return "GovCircuitBreaker";
  }

  @Override
  public boolean legal() {
    return true;
  }

  @Override
  public boolean simple() {
    return false;
  }
}
