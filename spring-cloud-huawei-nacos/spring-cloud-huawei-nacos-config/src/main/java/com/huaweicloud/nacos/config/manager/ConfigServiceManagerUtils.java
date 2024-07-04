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

package com.huaweicloud.nacos.config.manager;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConfigServiceManagerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceManagerUtils.class);

  public static boolean checkServerConnect(String serverAddress) {
    if (StringUtils.isEmpty(serverAddress)) {
      return false;
    }
    URI ipPort = parseIpPortFromURI(serverAddress);
    if (ipPort != null && ipPort.getHost() != null) {
      try (Socket s = new Socket()) {
        s.connect(new InetSocketAddress(ipPort.getHost(), ipPort.getPort()), 3000);
        return true;
      } catch (IOException e) {
        LOGGER.warn("ping endpoint {} failed, It will be quarantined again.", serverAddress);
      }
    }
    return false;
  }

  private static URI parseIpPortFromURI(String uri) {
    try {
      return new URI(uri);
    } catch (URISyntaxException e) {
      return null;
    }
  }
}
