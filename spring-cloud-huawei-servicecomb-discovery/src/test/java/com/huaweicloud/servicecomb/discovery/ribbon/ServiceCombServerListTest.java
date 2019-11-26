package com.huaweicloud.servicecomb.discovery.ribbon;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.cloud.client.ServiceInstance;
import com.huaweicloud.common.exception.ServiceCombException;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.model.Microservice;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

import com.netflix.client.config.IClientConfig;

import mockit.Expectations;
import mockit.Injectable;
import mockit.Tested;

/**
 * @Author wangqijun
 * @Date 11:36 2019-08-16
 **/
public class ServiceCombServerListTest {

  @Tested
  ServiceCombServerList serviceCombServerList;

  @Injectable
  ServiceCombClient serviceCombClient;

  @Injectable
  ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  @Injectable
  IClientConfig iClientConfig;

  @Test
  public void getInitialListOfServers() throws ServiceCombException {
    List<ServiceInstance> instanceList = new ArrayList<>();
    ServiceInstance serviceInstance = new ServiceInstance() {
      @Override
      public String getServiceId() {
        return "serviceid11";
      }

      @Override
      public String getHost() {
        return null;
      }

      @Override
      public int getPort() {
        return 0;
      }

      @Override
      public boolean isSecure() {
        return false;
      }

      @Override
      public URI getUri() {
        return null;
      }

      @Override
      public Map<String, String> getMetadata() {
        return null;
      }
    };
    instanceList.add(serviceInstance);
    new Expectations() {
      {
        iClientConfig.getClientName();
        result = "serviceid11";
        serviceCombClient.getInstances((Microservice) any);
        result = instanceList;
      }
    };

    serviceCombServerList.initWithNiwsConfig(iClientConfig);
    List<ServiceCombServer> serverlist = serviceCombServerList.getInitialListOfServers();
    Assert.assertEquals(serverlist.size(), 1);
  }
}