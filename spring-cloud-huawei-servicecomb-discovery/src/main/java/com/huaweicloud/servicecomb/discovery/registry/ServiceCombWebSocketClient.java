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

import com.huaweicloud.servicecomb.discovery.event.ServerCloseEvent;
import com.huaweicloud.servicecomb.discovery.event.ServerListRefreshEvent;
import com.huaweicloud.servicecomb.discovery.event.ServiceCombEvent;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.function.Consumer;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author GuoYl123
 * @Date 2020/4/23
 **/
public class ServiceCombWebSocketClient extends WebSocketClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombWebSocketClient.class);

  Consumer<ServiceCombEvent> publisher;

  public ServiceCombWebSocketClient(String serverUri, Map<String, String> map,
      Consumer<ServiceCombEvent> publisher) throws URISyntaxException {
    super(new URI(serverUri), new Draft_6455(), map, 60);
    this.publisher = publisher;
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
    LOGGER.info("watching microservice successfully.");
  }

  @Override
  public void onMessage(String s) {
    LOGGER.info("instance change : {}", s);
    publisher.accept(new ServerListRefreshEvent());
  }

  @Override
  public void onClose(int i, String s, boolean b) {
    LOGGER.warn("connection is closed accidentally, code : {}, reason : {}, remote : {}", i, s, b);
    publisher.accept(new ServerCloseEvent());
  }

  @Override
  public void onError(Exception e) {
    LOGGER.error("connection error , msg:{} ", e.getMessage());
    publisher.accept(new ServerCloseEvent());
  }
}