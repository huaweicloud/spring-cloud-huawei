package org.springframework.cloud.servicecomb.discovery.client.model;

import java.util.List;

/**
 * @Author wangqijun
 * @Date 18:44 2019-08-23
 **/
public class MicroserviceResponse {
  List<Microservice> services;

  public List<Microservice> getServices() {
    return services;
  }

  public void setServices(List<Microservice> services) {
    this.services = services;
  }
}
