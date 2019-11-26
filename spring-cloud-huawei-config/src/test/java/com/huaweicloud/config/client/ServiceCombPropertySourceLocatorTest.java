package com.huaweicloud.config.client;


import com.huaweicloud.config.ServiceCombConfigProperties;
import com.huaweicloud.config.ServiceCombPropertySourceLocator;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import com.huaweicloud.config.ServiceCombConfigProperties.Watch;
import com.huaweicloud.config.client.ConfigConstants;
import com.huaweicloud.config.client.ServiceCombConfigClient;
import org.springframework.core.env.Environment;
import org.springframework.core.env.PropertySource;

import mockit.Injectable;
import mockit.integration.junit4.JMockit;

/**
 * @Author wangqijun
 * @Date 17:38 2019-10-26
 **/
@RunWith(JMockit.class)
public class ServiceCombPropertySourceLocatorTest {

  @Test
  public void locate(
      @Injectable ServiceCombConfigClient serviceCombConfigClient,
      @Injectable Environment environment) {
    ServiceCombConfigProperties serviceCombConfigProperties = new ServiceCombConfigProperties();
    serviceCombConfigProperties.setEnable(true);
    serviceCombConfigProperties.setServerAddr("http://ddd");
    Watch watch = new Watch();
    watch.setEnable(true);
    watch.setWaitTime(1000);
    Assert.assertEquals(watch.getDelay(), 10 * 1000);
    watch.setDelay(10);
    Assert.assertEquals(watch.getDelay(), 10);
    serviceCombConfigProperties.setWatch(watch);
    ServiceCombPropertySourceLocator serviceCombPropertySourceLocator = new ServiceCombPropertySourceLocator(
        serviceCombConfigProperties, serviceCombConfigClient, "default");
    PropertySource<?> result = serviceCombPropertySourceLocator.locate(environment);
    Assert.assertEquals(result.getName(), ConfigConstants.PROPERTYSOURCE_NAME);
  }
}