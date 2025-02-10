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

import static com.alibaba.nacos.api.PropertyKeyConst.USERNAME;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.nacos.client.auth.impl.NacosAuthLoginConstant;
import com.alibaba.nacos.client.auth.impl.process.HttpLoginProcessor;
import com.alibaba.nacos.client.config.impl.ConfigHttpClientManager;
import com.alibaba.nacos.common.http.HttpRestResult;
import com.alibaba.nacos.common.http.client.NacosRestTemplate;
import com.alibaba.nacos.common.http.param.Header;
import com.alibaba.nacos.common.http.param.Query;
import com.alibaba.nacos.plugin.auth.api.LoginIdentityContext;

public class ConfigServiceManagerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(ConfigServiceManagerUtils.class);

  private static final Map<String, String> address_token = new ConcurrentHashMap<>();

  private static final String NACOS_CONFIG_HEALTH_CHECK_URI = "%s/nacos/v1/console/health/config/readiness";

  private static long tokenTtl;

  private static long lastRefreshTime;

  private static long refreshWindow;

  private static NacosRestTemplate nacosRestTemplate;

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
      String realUri = uri.startsWith("http") ? uri : "http://" + uri;
      return new URI(realUri);
    } catch (URISyntaxException e) {
      return null;
    }
  }

  public static NacosConfigManager chooseConfigManager(List<NacosConfigManager> nacosConfigManagers) {
    for (NacosConfigManager nacosConfigManager : nacosConfigManagers) {
      if (nacosConfigManager.isNacosServerHealth()) {
        return nacosConfigManager;
      }
      LOGGER.warn("nacos server [{}] unavailable, choose others.", nacosConfigManager.getServerAddr());
    }
    LOGGER.warn("all nacos server unavailable, use master server.");

    // if all server unavailable, return master server, ensure listening configuration when service is available again.
    return nacosConfigManagers.get(0);
  }

  public static String buildUrl(String address, String uri) {
    String prefix = "";
    if (!address.startsWith("http")) {
      prefix = "http://";
    }
    return prefix + String.format(uri, address);
  }

  public static Header initHeader(String address, String userName, Properties properties) {
    Header header = Header.newInstance();
    if (!StringUtils.isEmpty(userName)) {
      header.addParam(NacosAuthLoginConstant.ACCESSTOKEN, getAccessToken(address, properties));
    }
    return header;
  }

  private static String getAccessToken(String address, Properties properties) {
    if (address_token.get(address) != null
        && (System.currentTimeMillis() - lastRefreshTime) < TimeUnit.SECONDS.toMillis(tokenTtl - refreshWindow)) {
      return address_token.get(address);
    }
    HttpLoginProcessor httpLoginProcessor
        = new HttpLoginProcessor(ConfigHttpClientManager.getInstance().getNacosRestTemplate());
    properties.setProperty(NacosAuthLoginConstant.SERVER, address);
    LoginIdentityContext identityContext = httpLoginProcessor.getResponse(properties);
    if (identityContext != null
        && !StringUtils.isEmpty(identityContext.getParameter(NacosAuthLoginConstant.ACCESSTOKEN))) {
      tokenTtl = Long.parseLong(identityContext.getParameter(NacosAuthLoginConstant.TOKENTTL));
      refreshWindow = tokenTtl / 10;
      lastRefreshTime = System.currentTimeMillis();
      address_token.put(address, identityContext.getParameter(NacosAuthLoginConstant.ACCESSTOKEN));
      return identityContext.getParameter(NacosAuthLoginConstant.ACCESSTOKEN);
    }
    lastRefreshTime = System.currentTimeMillis();
    return "";
  }

  public static boolean checkConfigServerHealth(String serverAddr, Properties properties) {
    String url = buildUrl(serverAddr, NACOS_CONFIG_HEALTH_CHECK_URI);
    Header header = initHeader(serverAddr, properties.getProperty(USERNAME), properties);
    try {
      HttpRestResult<String> response = getNacosRestTemplate().get(url, header, new Query(), String.class);
      if (response.ok()) {
        return true;
      }
      if (response.getCode() == HttpStatus.SC_NOT_FOUND && checkServerConnect(serverAddr)) {
        return true;
      }
    } catch (Exception e) {
      LOGGER.error("check server [{}] health failed.", serverAddr);
    }
    return false;
  }

  public static NacosRestTemplate getNacosRestTemplate() {
    if (nacosRestTemplate == null) {
      nacosRestTemplate = ConfigHttpClientManager.getInstance().getNacosRestTemplate();
    }
    return nacosRestTemplate;
  }
}
