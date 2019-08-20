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

package org.springframework.cloud.servicecomb.discovery.client;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

public class ServiceCombClientBuilder {
  private String url;

  private boolean autoDiscovery;

  /**
   * registry address, e.g. http://127.0.0.1:30100
   * @param url registry address
   * @return
   */
  public ServiceCombClientBuilder setUrl(String url) {
    this.url = url;
    return this;
  }

  public ServiceCombClientBuilder setAutoDiscovery(boolean autoDiscovery) {
    this.autoDiscovery = autoDiscovery;
    return this;
  }

  /**
   * create ServiceComb-Service-Center client, ServiceCombClient is singleton.
   * @return
   */
  public ServiceCombClient createServiceCombClient() {
    DefaultHttpTransport httpTransport = DefaultHttpTransport.getInstance();
    return new ServiceCombClient(url, httpTransport, autoDiscovery);
  }
}