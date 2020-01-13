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

package com.huaweicloud.common.util;

import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;

import org.apache.http.client.utils.URIBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author wangqijun
 * @Date 10:50 2019-07-09
 **/
public class NetUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);

  /**
   * getLocalHost
   * @return
   */
  public static String getLocalHost() {
    InetAddress address;
    try {
      address = InetAddress.getLocalHost();
      return address.getHostName();
    } catch (UnknownHostException e) {
      LOGGER.error(e.getMessage(), e);
    }
    return null;
  }

  public static Integer getPort(String url) {
    URIBuilder endpointURIBuilder = null;
    Integer port;
    try {
      endpointURIBuilder = new URIBuilder(url);
      port = endpointURIBuilder.build().getPort();
      return port;
    } catch (URISyntaxException e) {
      LOGGER.error(e.getMessage(), e);
    }
    return null;
  }

  public static String getHost(String url) {
    URIBuilder endpointURIBuilder = null;
    String host = null;
    try {
      endpointURIBuilder = new URIBuilder(url);
      host = endpointURIBuilder.build().getHost();
      return host;
    } catch (URISyntaxException e) {
      LOGGER.error(e.getMessage(), e);
    }
    return host;
  }
}
