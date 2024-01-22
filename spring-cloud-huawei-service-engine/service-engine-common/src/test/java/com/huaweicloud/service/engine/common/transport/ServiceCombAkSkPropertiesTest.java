/*

  * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

package com.huaweicloud.service.engine.common.transport;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.huaweicloud.service.engine.common.EngineCommonConfiguration;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombAkSkProperties;

@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = {EngineCommonConfiguration.class, AkSkTestConfiguration.class})
@ContextConfiguration(initializers = ConfigDataApplicationContextInitializer.class)
public class ServiceCombAkSkPropertiesTest {
  @Autowired
  private ServiceCombAkSkProperties akSkProperties;

  @Autowired
  private Environment environment;

  @Test
  public void testConfigurationCorrect() {
    Assertions.assertNotNull(akSkProperties);
    Assertions.assertTrue(akSkProperties.isEnabled());
    Assertions.assertEquals("your ak", akSkProperties.getAccessKey());
    Assertions.assertEquals("1dabecbf115955223ea3b7348366c162678688b0fc70e282c0a33e6e4baeccb9",
        akSkProperties.getSecretKey());
    Assertions.assertEquals("cn-east-2", akSkProperties.getProject());
    Assertions.assertEquals("CustomCipher", akSkProperties.getCipher());
  }
}
