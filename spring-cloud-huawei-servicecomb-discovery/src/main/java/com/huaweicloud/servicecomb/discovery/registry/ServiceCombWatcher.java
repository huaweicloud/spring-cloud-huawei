package com.huaweicloud.servicecomb.discovery.registry;

import com.huaweicloud.common.transport.BackOff;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import com.huaweicloud.servicecomb.discovery.event.ServerCloseEvent;
import com.huaweicloud.servicecomb.discovery.event.ServiceCombEventBus;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.java_websocket.WebSocket;
import org.java_websocket.WebSocket.READYSTATE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author GuoYl123
 * @Date 2020/4/23
 **/
public class ServiceCombWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombWatcher.class);

  @Autowired
  private ServiceCombEventBus eventBus;

  private ServiceCombWebSocketClient webSocketClient;

  private ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, (r) -> {
    Thread thread = new Thread(r);
    thread.setName("com.huaweicloud.servercenter.watch");
    thread.setDaemon(true);
    return thread;
  });

  public void start(String url) {
    try {
      Map<String, String> signedHeader = new HashMap<>();
      signedHeader.put("x-domain-name", ServiceRegistryConfig.DEFAULT_PROJECT);
      webSocketClient = new ServiceCombWebSocketClient(url, signedHeader, eventBus::publish);
      connect();
      eventBus.register((event) -> {
        if (!(event instanceof ServerCloseEvent)) {
          return;
        }
        LOGGER.info("will retry to establish websocket connecting.");
        connect();
      });
    } catch (URISyntaxException e) {
      LOGGER.error("parse url error");
    }
  }

  private void connect() {
    EXECUTOR.execute(() -> {
      BackOff backOff = new BackOff();
      while (!webSocketClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
        if (!webSocketClient.getReadyState().equals(READYSTATE.CONNECTING)) {
          webSocketClient.connect();
        }
        LOGGER.debug("trying to establish websocket connecting.");
        backOff.waitingAndBackoff();
      }
    });
  }
}
