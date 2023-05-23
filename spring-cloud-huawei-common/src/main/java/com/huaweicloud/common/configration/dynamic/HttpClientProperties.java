/*
 * Copyright 2013-2022 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huaweicloud.common.configration.dynamic;

public class HttpClientProperties {
  private int connectionRequestTimeoutInMilliSeconds = 10000;

  private int connectTimeoutInMilliSeconds = 1000;

  private int readTimeoutInMilliSeconds = 30000;

  private int connectionIdleTimeoutInMilliSeconds = 30000;

  private int connectionTimeToLiveInMilliSeconds = -1;

  private int poolSizePerRoute = 50;

  private int poolSizeMax = 5000;

  public int getConnectionRequestTimeoutInMilliSeconds() {
    return connectionRequestTimeoutInMilliSeconds;
  }

  public void setConnectionRequestTimeoutInMilliSeconds(int connectionRequestTimeoutInMilliSeconds) {
    this.connectionRequestTimeoutInMilliSeconds = connectionRequestTimeoutInMilliSeconds;
  }

  public int getConnectTimeoutInMilliSeconds() {
    return connectTimeoutInMilliSeconds;
  }

  public void setConnectTimeoutInMilliSeconds(int connectTimeoutInMilliSeconds) {
    this.connectTimeoutInMilliSeconds = connectTimeoutInMilliSeconds;
  }

  public int getReadTimeoutInMilliSeconds() {
    return readTimeoutInMilliSeconds;
  }

  public void setReadTimeoutInMilliSeconds(int readTimeoutInMilliSeconds) {
    this.readTimeoutInMilliSeconds = readTimeoutInMilliSeconds;
  }

  public int getPoolSizePerRoute() {
    return poolSizePerRoute;
  }

  public void setPoolSizePerRoute(int poolSizePerRoute) {
    this.poolSizePerRoute = poolSizePerRoute;
  }

  public int getPoolSizeMax() {
    return poolSizeMax;
  }

  public void setPoolSizeMax(int poolSizeMax) {
    this.poolSizeMax = poolSizeMax;
  }

  public int getConnectionIdleTimeoutInMilliSeconds() {
    return connectionIdleTimeoutInMilliSeconds;
  }

  public void setConnectionIdleTimeoutInMilliSeconds(int connectionIdleTimeoutInMilliSeconds) {
    this.connectionIdleTimeoutInMilliSeconds = connectionIdleTimeoutInMilliSeconds;
  }

  public int getConnectionTimeToLiveInMilliSeconds() {
    return connectionTimeToLiveInMilliSeconds;
  }

  public void setConnectionTimeToLiveInMilliSeconds(int connectionTimeToLiveInMilliSeconds) {
    this.connectionTimeToLiveInMilliSeconds = connectionTimeToLiveInMilliSeconds;
  }
}
