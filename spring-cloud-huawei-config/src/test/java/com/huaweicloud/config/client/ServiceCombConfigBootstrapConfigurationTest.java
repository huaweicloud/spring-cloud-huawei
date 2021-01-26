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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import com.huaweicloud.common.CommonConfiguration;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import com.huaweicloud.config.ServiceCombConfigBootstrapConfiguration;
import com.huaweicloud.config.ServiceCombConfigProperties;

/**
 * @Author wangqijun
 * @Date 20:05 2019-10-27
 **/
public class ServiceCombConfigBootstrapConfigurationTest {
  private final ApplicationContextRunner contextRunner = new ApplicationContextRunner();

  @Test
  public void serviceCombPropertySourceLocator() {
    this.contextRunner.withUserConfiguration(ServiceCombConfigBootstrapConfiguration.class, CommonConfiguration.class,
        ServiceCombAkSkProperties.class)
        .run(context -> {
          ServiceCombConfigProperties serviceCombConfigProperties = context.getBean(ServiceCombConfigProperties.class);
          assertThat(serviceCombConfigProperties.isEnabled())
              .isEqualTo(true);
        });
  }
}
