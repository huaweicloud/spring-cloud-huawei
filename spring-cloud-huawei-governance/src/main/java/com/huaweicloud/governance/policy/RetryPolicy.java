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
 *  intervalFunction  失败时可以更改等待时间的函数
 *  retryOnResultPredicate  根据返回结果决定是否进行重试
 *  retryOnExceptionPredicate  根据失败异常决定是否进行重试
 *
 */
public class RetryPolicy extends AbstractPolicy {
  //最多尝试次数
  private Integer maxAttempts;

  //每次重试尝试等待的时间，默认给0
  private String waitDuration;

  private String retryOnResponseStatus;

  //需要进行重试的异常列表，反射取异常
  private String retryExceptions;

  //需要进行忽略的异常列表
  private String ignoreExceptions;

  private boolean onSame;

  public String getRetryOnResponseStatus() {
    return retryOnResponseStatus;
  }

  public void setRetryOnResponseStatus(String retryOnResponseStatus) {
    this.retryOnResponseStatus = retryOnResponseStatus;
  }

  public Integer getMaxAttempts() {
    return maxAttempts;
  }

  public void setMaxAttempts(Integer maxAttempts) {
    this.maxAttempts = maxAttempts;
  }

  public String getWaitDuration() {
    return waitDuration;
  }

  public void setWaitDuration(String waitDuration) {
    this.waitDuration = waitDuration;
  }

  public String getRetryExceptions() {
    return retryExceptions;
  }

  public void setRetryExceptions(String retryExceptions) {
    this.retryExceptions = retryExceptions;
  }

  public String getIgnoreExceptions() {
    return ignoreExceptions;
  }

  public void setIgnoreExceptions(String ignoreExceptions) {
    this.ignoreExceptions = ignoreExceptions;
  }

  public boolean isOnSame() {
    return onSame;
  }

  public void setOnSame(boolean onSame) {
    this.onSame = onSame;
  }

  @Override
  public String handler() {
    return "GovRetry";
  }

  //todo
  @Override
  public boolean legal() {
    return true;
  }

  @Override
  public boolean simple() {
    return false;
  }

}
