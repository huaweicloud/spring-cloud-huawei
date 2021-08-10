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

package com.huaweicloud.common.transport;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import com.huaweicloud.common.CommonConfiguration;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {CommonConfiguration.class, AkSkTestConfiguration.class})
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
public class ServiceCombAkSkPropertiesTest {
  @Autowired
  private ServiceCombAkSkProperties akSkProperties;

  @Autowired
  private Environment environment;

  @Test
  public void testConfigurationCorrect() {
    Assert.assertNotNull(akSkProperties);
    Assert.assertEquals(true, akSkProperties.isEnabled());
    Assert.assertEquals("your ak", akSkProperties.getAccessKey());
    Assert.assertEquals("1dabecbf115955223ea3b7348366c162678688b0fc70e282c0a33e6e4baeccb9",
        akSkProperties.getSecretKey());
    Assert.assertEquals("cn-east-2", akSkProperties.getProject());
    Assert.assertEquals("CustomCipher", akSkProperties.getCipher());
  }
}
