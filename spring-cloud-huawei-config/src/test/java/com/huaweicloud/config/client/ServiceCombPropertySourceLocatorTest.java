/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
    serviceCombConfigProperties.setEnabled(true);
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
