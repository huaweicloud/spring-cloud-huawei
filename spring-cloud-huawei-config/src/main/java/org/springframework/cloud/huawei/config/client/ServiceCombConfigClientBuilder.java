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

package org.springframework.cloud.huawei.config.client;

import org.springframework.cloud.common.transport.DefaultHttpTransport;
import org.springframework.cloud.common.transport.SSLConfig;

/**
 * @Author wangqijun
 * @Date 20:20 2019-10-19
 **/
public class ServiceCombConfigClientBuilder {

  private String url;

  private SSLConfig sslConfig;

  public ServiceCombConfigClientBuilder setSSLConfig(SSLConfig sslConfig) {
    this.sslConfig = sslConfig;
    return this;
  }

  public ServiceCombConfigClientBuilder setUrl(String url) {
    this.url = url;
    return this;
  }

  public ServiceCombConfigClient createServiceCombConfigClient() {
    DefaultHttpTransport httpTransport = DefaultHttpTransport.getInstance();
    httpTransport.setSslConfig(sslConfig);
    return new ServiceCombConfigClient(url, httpTransport);
  }
}
