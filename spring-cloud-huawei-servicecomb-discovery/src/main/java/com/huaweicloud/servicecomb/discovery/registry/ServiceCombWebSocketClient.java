package com.huaweicloud.servicecomb.discovery.registry;

import com.huaweicloud.servicecomb.discovery.event.ServerListRefreshEvent;
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

  Consumer<ServerListRefreshEvent> publisher;

  public ServiceCombWebSocketClient(String serverUri, Map<String, String> map,
      Consumer<ServerListRefreshEvent> publisher)
      throws URISyntaxException {
    super(new URI(serverUri), new Draft_6455(), map, 0);
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
    LOGGER.info("connection is closed accidentally, code : {}, reason : {}, remote : {}", i, s, b);
  }

  @Override
  public void onError(Exception e) {
    LOGGER.error("connection error ", e);
  }
}