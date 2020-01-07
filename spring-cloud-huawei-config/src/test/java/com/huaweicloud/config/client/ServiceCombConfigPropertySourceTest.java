package com.huaweicloud.config.client;

import com.huaweicloud.config.ServiceCombConfigProperties;
import com.huaweicloud.config.ServiceCombConfigPropertySource;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.config.client.ServiceCombConfigClient;

import mockit.Deencapsulation;
import mockit.Expectations;
import mockit.Injectable;
import mockit.MockUp;
import mockit.integration.junit4.JMockit;

/**
 * @Author wangqijun
 * @Date 17:43 2019-10-26
 **/
@RunWith(JMockit.class)
public class ServiceCombConfigPropertySourceTest extends MockUp<ServiceCombConfigPropertySource> {

  private Map<String, Object> properties = new HashMap<>();

  @Test
  public void loadAllRemoteConfig(@Injectable String name, @Injectable ServiceCombConfigClient source,
      @Injectable ServiceCombConfigProperties serviceCombConfigProperties)
      throws RemoteOperationException {
    name = "dd";
    Map<String, String> map = new HashMap<>();
    map.put("r", "r");
    map.put("d", "r");
    new Expectations() {
      {
        source.loadAll((ServiceCombConfigProperties) any, anyString);
        result = map;
      }
    };
    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(name, source);
    Deencapsulation.setField(serviceCombConfigPropertySource, properties);
    Map<String, String> result = serviceCombConfigPropertySource.loadAllRemoteConfig(serviceCombConfigProperties, "");
    Assert.assertEquals(result.size(), 2);
  }

  @Test
  public void getPropertyNames(@Injectable String name, @Injectable ServiceCombConfigClient source) {
    name = "dd";
    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(name, source);
    Deencapsulation.setField(serviceCombConfigPropertySource, properties);
    properties.put("test", "tt");
    properties.put("test2", "tt");
    String[] result = serviceCombConfigPropertySource.getPropertyNames();
    Assert.assertEquals(result.length, 2);
  }

  @Test
  public void getProperty(@Injectable String name, @Injectable ServiceCombConfigClient source) {
    name = "dd";
    ServiceCombConfigPropertySource serviceCombConfigPropertySource = new ServiceCombConfigPropertySource(name, source);
    Deencapsulation.setField(serviceCombConfigPropertySource, properties);
    properties.put("test", "tt");
    Object result = serviceCombConfigPropertySource.getProperty("test");
    Assert.assertEquals(result, "tt");
  }
}