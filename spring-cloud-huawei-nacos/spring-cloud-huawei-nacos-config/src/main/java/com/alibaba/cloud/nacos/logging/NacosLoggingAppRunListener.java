/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.logging;

import com.alibaba.nacos.client.logging.NacosLogging;

import org.springframework.boot.ConfigurableBootstrapContext;
import org.springframework.boot.SpringApplicationRunListener;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Forked from com.alibaba.cloud.nacos.client.NacosLoggingAppRunListener.java
 *
 * Reload nacos log configuration on spring application contextPrepared.
 *
 * @author RuanSheng
 */
public class NacosLoggingAppRunListener implements SpringApplicationRunListener, Ordered {
  @Override
  public void environmentPrepared(ConfigurableBootstrapContext bootstrapContext, ConfigurableEnvironment environment) {
    NacosLogging.getInstance().loadConfiguration();
  }

  @Override
  public void contextPrepared(ConfigurableApplicationContext context) {
    NacosLogging.getInstance().loadConfiguration();
  }

  @Override
  public int getOrder() {
    return 1;
  }
}
