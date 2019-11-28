package com.huaweicloud.servicecomb.discovery.registry;

import org.junit.Test;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

import mockit.Injectable;

/**
 * @Author wangqijun
 * @Date 11:16 2019-08-16
 **/
public class HeartbeatSchedulerTest {

  @Injectable
  ServiceCombClient serviceCombClient;

  @Injectable
  ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  @Test
  public void addAndRemove() {
    serviceCombDiscoveryProperties.setHealthCheckInterval(10);
    HeartbeatScheduler heartbeatScheduler = new HeartbeatScheduler(serviceCombDiscoveryProperties, serviceCombClient);
    heartbeatScheduler.add("11", "22");
    heartbeatScheduler.remove("11");
  }
}