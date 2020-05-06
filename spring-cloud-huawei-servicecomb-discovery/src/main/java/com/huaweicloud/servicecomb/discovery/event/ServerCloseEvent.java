package com.huaweicloud.servicecomb.discovery.event;

/**
 * @Author GuoYl123
 * @Date 2020/4/27
 **/
public class ServerCloseEvent implements ServiceCombEvent {

  private Object source;

  public ServerCloseEvent() {
  }

  public ServerCloseEvent(Object source) {
    this.source = source;
  }

  @Override
  public Object getSource() {
    return null;
  }
}
