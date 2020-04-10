package com.huaweicloud.servicecomb.discovery.registry;

import com.huaweicloud.common.transport.BackOff;
import com.huaweicloud.common.util.URLUtil;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import com.netflix.loadbalancer.DynamicServerListLoadBalancer;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import org.java_websocket.WebSocket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2020/4/18
 **/
public class ServiceCenterWatcher {

  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCenterWatcher.class);

  private DynamicServerListLoadBalancer lb;

  private ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, (r) -> {
    Thread thread = new Thread(r);
    thread.setName("com.huaweicloud.servercenter.watch");
    thread.setDaemon(true);
    return thread;
  });

  public ServiceCenterWatcher(DynamicServerListLoadBalancer lb) {
    this.lb = lb;
  }

  public void start(String ipHost, String serviceID) {
    if (StringUtils.isEmpty(ipHost)) {
      return;
    }
    String[] urls = URLUtil.splitIpPort(ipHost);
    String url = "ws://" + urls[0] + ":" + urls[1] + "/v4/" + ServiceRegistryConfig.DEFAULT_PROJECT
        + "/registry/microservices/" + serviceID + "/watcher";
    EXECUTOR.execute(() -> {
      Map<String, String> map = new HashMap<>();
      map.put("x-domain-name", ServiceRegistryConfig.DEFAULT_PROJECT);
      ServiceCenterWebSocketClient client = null;
      try {
        client = new ServiceCenterWebSocketClient(url, map);
      } catch (URISyntaxException e) {
        e.printStackTrace();
      }
      client.setLb(lb);
      client.connect();
      BackOff backOff = new BackOff();
      while (!client.getReadyState().equals(WebSocket.READYSTATE.OPEN)) {
        LOGGER.debug("websocket is not ok.");
        backOff.waitingAndBackoff();
      }
      LOGGER.info("watching microservice {} successfully, "
          + "the chosen service center address is {}", serviceID, ipHost);
    });
  }
}
