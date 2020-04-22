package com.huaweicloud.servicecomb.discovery.event;

import java.util.ArrayList;
import java.util.List;

/**
 * @Author GuoYl123
 * @Date 2020/4/20
 **/
public class ServiceCombEventBus {

  private List<ServiceCombListener> listeners = new ArrayList<>();

  public void register(ServiceCombListener listener) {
    listeners.add(listener);
  }

  public void publish(ServerListRefreshEvent event) {
    listeners.forEach(l -> l.onEvent(event));
  }
}
