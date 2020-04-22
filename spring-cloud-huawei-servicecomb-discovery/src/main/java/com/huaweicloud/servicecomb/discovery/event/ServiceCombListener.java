package com.huaweicloud.servicecomb.discovery.event;

/**
 * @Author GuoYl123
 * @Date 2020/4/20
 **/
public interface ServiceCombListener {

  void onEvent(ServerListRefreshEvent event);
}
