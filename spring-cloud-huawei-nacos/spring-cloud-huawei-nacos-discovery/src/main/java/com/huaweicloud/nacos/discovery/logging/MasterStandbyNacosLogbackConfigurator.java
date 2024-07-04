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

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;

public class MasterStandbyNacosLogbackConfigurator extends JoranConfigurator {
  public void setContext(LoggerContext loggerContext) {
    super.setContext(loggerContext);
  }

  public void configure(URL url) throws Exception {
    URLConnection urlConnection = url.openConnection();
    urlConnection.setUseCaches(false);
    try (InputStream in = urlConnection.getInputStream()) {
      doConfigure(in, url.toExternalForm());
    } catch (IOException ioException) {
      addError("open URL [" + url + "] failed.", ioException);
      throw new JoranException("open URL [" + url + "] failed.", ioException);
    }
  }
}
