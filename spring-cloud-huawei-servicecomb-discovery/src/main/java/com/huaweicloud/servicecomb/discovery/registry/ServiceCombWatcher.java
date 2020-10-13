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

package com.huaweicloud.servicecomb.discovery.registry;

import com.huaweicloud.common.cache.TokenCache;
import com.huaweicloud.common.transport.BackOff;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import com.huaweicloud.common.util.SecretUtil;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import com.huaweicloud.servicecomb.discovery.event.ServerCloseEvent;
import com.huaweicloud.servicecomb.discovery.event.ServiceCombEventBus;

import java.net.URISyntaxException;
import java.security.KeyManagementException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.java_websocket.client.WebSocketClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author GuoYl123
 * @Date 2020/4/23
 **/
public class ServiceCombWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombWatcher.class);

  private static final String SSL_PREFIX = "wss://";

  private static final String DEFAULT_PREFIX = "ws://";

  private ServiceCombEventBus eventBus;

  private ServiceCombSSLProperties serviceCombSSLProperties;

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  private String url;

  private BackOff backOff = new BackOff(5000);

  private SSLContext sslContext;

  private ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, (r) -> {
    Thread thread = new Thread(r);
    thread.setName("com.huaweicloud.servercenter.watch");
    thread.setDaemon(true);
    return thread;
  });

  public ServiceCombWatcher(ServiceCombEventBus eventBus,
      ServiceCombSSLProperties serviceCombSSLProperties,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    this.eventBus = eventBus;
    this.serviceCombSSLProperties = serviceCombSSLProperties;
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
  }

  public void start(String url) {
    if (serviceCombDiscoveryProperties.getAddress().contains("https")) {
      this.url = SSL_PREFIX + url;
      initSSL();
    } else {
      this.url = DEFAULT_PREFIX + url;
    }
    connect();
    eventBus.register((event) -> {
      if (!(event instanceof ServerCloseEvent)) {
        return;
      }
      LOGGER.info("retrying to establish websocket connecting.");
      connect();
    });
  }

  private synchronized void connect() {
    WebSocketClient webSocketClient = buildClient();
    EXECUTOR.execute(() -> {
      if (webSocketClient == null) {
        return;
      }
      try {
        webSocketClient.connect();
      } catch (IllegalStateException e) {
        LOGGER.debug("establish websocket connect failed.", e);
        return;
      }
      backOff.waitingAndBackoff();
    });
  }

  private void initSSL() {
    sslContext = SecretUtil.getSSLContext(serviceCombSSLProperties);
    try {
      sslContext.init(null, new TrustManager[] {new X509TrustManager() {
        @Override
        public void checkClientTrusted(X509Certificate[] chain,
            String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain,
            String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
          return new X509Certificate[0];
        }
      }}, new SecureRandom());
    } catch (KeyManagementException e) {
      LOGGER.error("websocket ssl init failed.");
    }
  }

  private WebSocketClient buildClient() {
    Map<String, String> signedHeader = new HashMap<>();
    signedHeader.put("x-domain-name", ServiceRegistryConfig.DEFAULT_PROJECT);
    if (TokenCache.getToken() != null) {
      signedHeader.put("Authorization", "Bearer " + TokenCache.getToken().getToken());
    }
    WebSocketClient webSocketClient;
    try {
      webSocketClient = new ServiceCombWebSocketClient(url, signedHeader, eventBus::publish);
    } catch (URISyntaxException e) {
      LOGGER.error("parse url error");
      return null;
    }
    if (sslContext != null) {
      webSocketClient.setSocketFactory(sslContext.getSocketFactory());
    }
    return webSocketClient;
  }
}
