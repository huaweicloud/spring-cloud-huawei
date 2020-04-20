package com.huaweicloud.servicecomb.discovery.discovery;

import com.huaweicloud.servicecomb.discovery.event.ServerListRefreshEvent;
import com.huaweicloud.servicecomb.discovery.event.ServiceCombEventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

/**
 * @Author GuoYl123
 * @Date 2020/4/20
 **/
public class ServerListRefreshHandler implements WebSocketHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServerListRefreshHandler.class);

  @Autowired
  private ServiceCombEventBus eventBus;

  @Override
  public void afterConnectionEstablished(WebSocketSession session) throws Exception {
    LOGGER.info("watching microservice successfully, "
        + "the chosen service center address is {}", session.getRemoteAddress());
  }

  @Override
  public void handleMessage(WebSocketSession session, WebSocketMessage<?> message)
      throws Exception {
    LOGGER.info("instance change : {}", message.getPayload());
    eventBus.publish(new ServerListRefreshEvent());
  }

  @Override
  public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
    LOGGER.error("connection error ", exception);
  }

  @Override
  public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
    LOGGER.info("connection is closed accidentally, status : {}, remote : {}", status,
        session.getRemoteAddress());
  }

  @Override
  public boolean supportsPartialMessages() {
    return false;
  }

}
