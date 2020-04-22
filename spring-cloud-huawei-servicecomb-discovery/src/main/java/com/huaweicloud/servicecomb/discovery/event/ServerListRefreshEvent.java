package com.huaweicloud.servicecomb.discovery.event;

/**
 * @Author GuoYl123
 * @Date 2020/4/20
 **/
public class ServerListRefreshEvent {

  private Object source;

  public ServerListRefreshEvent() {
  }

  public ServerListRefreshEvent(Object source) {
    this.source = source;
  }

  public Object getSource() {
    return source;
  }
}
