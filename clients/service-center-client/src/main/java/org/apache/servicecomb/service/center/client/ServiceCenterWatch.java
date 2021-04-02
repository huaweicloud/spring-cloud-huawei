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

package org.apache.servicecomb.service.center.client;

import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.http.client.auth.RequestAuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpConfiguration.SSLProperties;
import org.apache.servicecomb.http.client.common.WebSocketListener;
import org.apache.servicecomb.http.client.common.WebSocketTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServiceCenterWatch implements WebSocketListener {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterWatch.class);

  private AddressManager addressManager;

  private SSLProperties sslProperties;

  private RequestAuthHeaderProvider requestAuthHeaderProvider;

  private String tenantName;

  private Map<String, String> extraGlobalHeaders;

  private WebSocketTransport webSocketTransport;

  public ServiceCenterWatch(AddressManager addressManager,
      SSLProperties sslProperties,
      RequestAuthHeaderProvider requestAuthHeaderProvider,
      String tenantName,
      Map<String, String> extraGlobalHeaders) {
    this.addressManager = addressManager;
    this.sslProperties = sslProperties;
    this.requestAuthHeaderProvider = requestAuthHeaderProvider;
    this.tenantName = tenantName;
    this.extraGlobalHeaders = extraGlobalHeaders;
  }

  public void startWatch() {
    try {
      Map<String, String> headers = new HashMap<>();
      headers.put("x-domain-name", this.tenantName);
      headers.putAll(this.extraGlobalHeaders);
      headers.putAll(this.requestAuthHeaderProvider.loadAuthHeader(null));
      webSocketTransport = new WebSocketTransport(addressManager.address(), sslProperties,
          headers, this);
      webSocketTransport.connect();
    } catch (URISyntaxException e) {
      LOGGER.error("start watch failed. ", e);
    }
  }

  public void stop() {
    if (webSocketTransport != null) {
      webSocketTransport.close();
    }
  }
  
  private void reconnect() {
    if (webSocketTransport != null) {
      webSocketTransport.close();
    }
    addressManager.changeAddress();
    startWatch();
  }

  @Override
  public void onMessage(String s) {
    LOGGER.info("web socket receive message [{}], start query instance", s);
  }

  @Override
  public void onError(Exception e) {
    LOGGER.warn("web socket receive error [{}], will restart.", e.getMessage());
    reconnect();
  }
}
