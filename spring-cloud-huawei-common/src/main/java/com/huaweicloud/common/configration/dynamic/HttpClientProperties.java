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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties("spring.cloud.servicecomb.httpclient")
public class HttpClientProperties {
  private int connectTimeoutInMilliSeconds = 1000;

  private int readTimeoutInMilliSeconds = 30000;

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
}
