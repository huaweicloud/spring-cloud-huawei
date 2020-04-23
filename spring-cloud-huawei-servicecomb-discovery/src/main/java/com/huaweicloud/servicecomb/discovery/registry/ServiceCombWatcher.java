package com.huaweicloud.servicecomb.discovery.registry;

import com.huaweicloud.common.transport.BackOff;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import com.huaweicloud.servicecomb.discovery.event.ServiceCombEventBus;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.java_websocket.WebSocket;
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

  private ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, (r) -> {
    Thread thread = new Thread(r);
    thread.setName("com.huaweicloud.servercenter.watch");
    thread.setDaemon(true);
    return thread;
  });

  private BackOff backOff = new BackOff();

  public void start(String url) {
    try {
      Map<String, String> signedHeader = new HashMap<>();
      signedHeader.put("x-domain-name", ServiceRegistryConfig.DEFAULT_PROJECT);
      ServiceCombWebSocketClient webSocketClient = new ServiceCombWebSocketClient(url, signedHeader,
          eventBus::publish);
      EXECUTOR.execute(() -> {
        webSocketClient.connect();
        while (!webSocketClient.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
          LOGGER.debug("websocket connect failed, will retry.");
          backOff.waitingAndBackoff();
        }
      });
    } catch (URISyntaxException e) {
      LOGGER.error("parse url error");
    }
  }
}
