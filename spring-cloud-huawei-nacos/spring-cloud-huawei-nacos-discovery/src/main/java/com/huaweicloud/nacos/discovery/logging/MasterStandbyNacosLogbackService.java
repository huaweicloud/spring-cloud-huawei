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

import java.io.FileNotFoundException;
import java.net.URL;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.LoggerContextListener;

public class MasterStandbyNacosLogbackService {
  private static final String NACOS_LOGBACK_RESOURCE = "master-standby-logback.xml";

  private final static MasterStandbyNacosLogbackService INSTANCE = new MasterStandbyNacosLogbackService();

  private MasterStandbyNacosLogbackService() {

  }

  public static MasterStandbyNacosLogbackService getInstance() {
    return INSTANCE;
  }

  public void loadConfiguration() {
    LoggerContext loggerContext = loadConfigurationOnStart();
    loggerContext.addListener(new MasterStandbyNacosLoggerContextListener());
  }

  private LoggerContext loadConfigurationOnStart() {
    LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
    MasterStandbyNacosLogbackConfigurator configurator = new MasterStandbyNacosLogbackConfigurator();
    try {
      configurator.setContext(loggerContext);
      configurator.configure(getResourceUrl());
    } catch (Exception e) {
      throw new IllegalStateException(
          "Could not initialize Logback logging from classpath:" + NACOS_LOGBACK_RESOURCE, e);
    }
    return loggerContext;
  }

  private URL getResourceUrl() throws FileNotFoundException {
    ClassLoader classLoader = MasterStandbyNacosLogbackConfigurator.class.getClassLoader();
    URL url = (classLoader != null ? classLoader.getResource(NACOS_LOGBACK_RESOURCE)
        : ClassLoader.getSystemResource(NACOS_LOGBACK_RESOURCE));
    if (url == null) {
      throw new FileNotFoundException("Resource [ classpath: " + NACOS_LOGBACK_RESOURCE + "] does not exist");
    }
    return url;
  }


  class MasterStandbyNacosLoggerContextListener implements LoggerContextListener {

    @Override
    public boolean isResetResistant() {
      return true;
    }

    @Override
    public void onReset(LoggerContext context) {
      loadConfigurationOnStart();
    }

    @Override
    public void onStart(LoggerContext context) {

    }

    @Override
    public void onStop(LoggerContext context) {

    }

    @Override
    public void onLevelChange(Logger logger, Level level) {

    }
  }
}
