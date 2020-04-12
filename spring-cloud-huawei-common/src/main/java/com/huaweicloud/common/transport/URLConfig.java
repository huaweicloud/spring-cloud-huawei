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

import com.huaweicloud.common.exception.ServiceCombRuntimeException;
import com.huaweicloud.common.util.URLUtil;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

/**
 * @Author GuoYl123
 * @Date 2019/12/19
 **/
public class URLConfig {

  private static final Logger LOGGER = LoggerFactory.getLogger(URLConfig.class);

  private List<String> urlList = new ArrayList<>();

  private int index = 0;

  private int resolveUrlSize = 0;

  private int afterDnsResolveIndex = 0;

  private static final int MAX_DELAY_TIME = 60 * 1000;

  private int retryDelayTime = 1000;

  public String getUrl() {
    if (isEmpty()) {
      throw new ServiceCombRuntimeException("no available address");
    }
    if (resolveUrlSize > 0) {
      String url = urlList.get(afterDnsResolveIndex);
      return url;
    }
    return urlList.get(index);
  }

  public void addUrl(List<String> urls) {
    if (CollectionUtils.isEmpty(urls)) {
      return;
    }
    urlList.addAll(urls);
  }

  public void addUrlAfterDnsResolve(String url) {
    if (StringUtils.isEmpty(url)) {
      return;
    }
    try (Socket s = new Socket()) {
      String[] ipPort = URLUtil.splitIpPort(url);
      s.connect(new InetSocketAddress(ipPort[0], Integer.parseInt(ipPort[1])), 3000);
    } catch (IOException e) {
      return;
    }
    LOGGER.info("choose auto discovery endpoint: {}", url);
    if (resolveUrlSize == 0) {
      afterDnsResolveIndex = urlList.size();
    }
    urlList.add(url);
    resolveUrlSize++;
  }

  public boolean isEmpty() {
    return urlList.isEmpty();
  }

  public synchronized void toggle() {
    if (isEmpty()) {
      throw new ServiceCombRuntimeException("no available address");
    }
    if (resolveUrlSize > 0) {
      afterDnsResolveIndex = afterDnsResolveIndex + 1 < urlList.size() ? afterDnsResolveIndex + 1
          : urlList.size() - resolveUrlSize;
      if (afterDnsResolveIndex == 0) {
        backOff();
      }
    } else {
      index = (index + 1) % urlList.size();
      if (index == 0) {
        backOff();
      }
    }
    try {
      Thread.sleep(retryDelayTime);
    } catch (InterruptedException e) {
      LOGGER.warn("thread interrupted.");
    }
  }

  public void backOff() {
    if (MAX_DELAY_TIME == retryDelayTime) {
      return;
    }
    retryDelayTime *= 2;
    if (MAX_DELAY_TIME <= retryDelayTime) {
      retryDelayTime = MAX_DELAY_TIME;
    }
  }
}
