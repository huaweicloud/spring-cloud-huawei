package com.huaweicloud.servicecomb.discovery.registry;

import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author GuoYl123
 * @Date 2020/4/18
 **/
public class ServiceCenterWebSocketClient extends WebSocketClient {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterWebSocketClient.class);

  private DynamicServerListLoadBalancer lb;

  public ServiceCenterWebSocketClient(String serverUri, Map<String, String> map)
      throws URISyntaxException {
    super(new URI(serverUri), new Draft_6455(), map, 0);
  }

  public void setLb(DynamicServerListLoadBalancer lb) {
    this.lb = lb;
  }

  @Override
  public void onOpen(ServerHandshake serverHandshake) {
  }

  @Override
  public void onMessage(String s) {
    LOGGER.debug("instance change : {}", s);
    //no matter what event , just refresh pull
    lb.updateListOfServers();
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
