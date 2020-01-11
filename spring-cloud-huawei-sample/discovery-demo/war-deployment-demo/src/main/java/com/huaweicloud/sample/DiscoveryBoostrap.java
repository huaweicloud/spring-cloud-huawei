package com.huaweicloud.sample;

import com.huaweicloud.servicecomb.discovery.registry.ServiceCombAutoServiceRegistration;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author GuoYl123
 * @Date 2020/1/11
 **/
@Component
public class DiscoveryBoostrap {

  @Autowired(required = false)
  private ServiceCombAutoServiceRegistration registration;

  @PostConstruct
  public void init() {
    registration.start();
  }
}
