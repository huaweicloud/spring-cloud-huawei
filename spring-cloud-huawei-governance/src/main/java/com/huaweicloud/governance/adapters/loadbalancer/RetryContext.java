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

package com.huaweicloud.governance.adapters.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;

public class RetryContext {
  public static final String RETRY_ITERATION = "x-r-iteration";

  public static final String RETRY_CONTEXT = "x-r-context";

  public static final String RETRY_SERVICE_INSTANCE = "x-r-instance";

  private boolean retry;

  private int triedCount;

  private int retryOnSame;

  private ServiceInstance lastServer;

  public RetryContext(int retryOnSame) {
    this.retryOnSame = retryOnSame;
    this.retry = false;
    this.triedCount = 0;
  }

  public boolean isRetry() {
    return retry;
  }

  public void incrementRetry() {
    this.retry = true;
    this.triedCount++;
  }

  public boolean trySameServer() {
    return triedCount < retryOnSame;
  }

  public ServiceInstance getLastServer() {
    return lastServer;
  }

  public void setLastServer(ServiceInstance lastServer) {
    this.lastServer = lastServer;
  }
}