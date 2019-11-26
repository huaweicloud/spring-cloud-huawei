package com.huaweicloud.servicecomb.discovery.client.model;

/**
 * @Author wangqijun
 * @Date 15:01 2019-08-22
 **/
public class MicroserviceInstanceSingleResponse {
  private MicroserviceInstance instance = null;

  public MicroserviceInstance getInstance() {
    return instance;
  }

  public void setInstance(MicroserviceInstance instance) {
    this.instance = instance;
  }
}
