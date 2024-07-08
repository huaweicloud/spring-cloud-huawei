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

package com.huaweicloud.nacos.discovery.logging;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

import com.huaweicloud.nacos.discovery.NacosConst;

public class MasterStandbyNacosLogbackAppRunListener implements SpringApplicationRunListener, Ordered {
  @SuppressWarnings("PMD")
  public MasterStandbyNacosLogbackAppRunListener(SpringApplication application, String[] args) {

  }
  
  @Override
  public int getOrder() {
    return 1;
  }

  @Override
  public void contextPrepared(ConfigurableApplicationContext context) {
    if (enabled(context.getEnvironment())) {
      MasterStandbyNacosLogbackService.getInstance().loadConfiguration();
    }
  }

  private boolean enabled(ConfigurableEnvironment environment) {
    return environment.getProperty(NacosConst.MASTER_STANDBY_ENABLED, boolean.class, false);
  }
}
