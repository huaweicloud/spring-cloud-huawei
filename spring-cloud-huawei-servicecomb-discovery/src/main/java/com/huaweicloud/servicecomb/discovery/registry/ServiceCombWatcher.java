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
import org.java_websocket.client.WebSocketClient;
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

  private String url;

  private BackOff backOff = new BackOff();

  private ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, (r) -> {
    Thread thread = new Thread(r);
    thread.setName("com.huaweicloud.servercenter.watch");
    thread.setDaemon(true);
    return thread;
  });

  public void start(String url) {
    this.url = url;
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

  private WebSocketClient buildClient() {
    Map<String, String> signedHeader = new HashMap<>();
    signedHeader.put("x-domain-name", ServiceRegistryConfig.DEFAULT_PROJECT);
    WebSocketClient webSocketClient;
    try {
      webSocketClient = new ServiceCombWebSocketClient(url, signedHeader, eventBus::publish);
    } catch (URISyntaxException e) {
      LOGGER.error("parse url error");
      return null;
    }
    return webSocketClient;
  }
}
