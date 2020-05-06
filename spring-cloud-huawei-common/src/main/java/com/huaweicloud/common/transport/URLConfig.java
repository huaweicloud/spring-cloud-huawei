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
import java.util.Random;
import java.util.stream.Collectors;
import org.springframework.util.CollectionUtils;

/**
 * @Author GuoYl123
 * @Date 2019/12/19
 **/
public class URLConfig {

  private List<String> urlList = new ArrayList<>();

  private int index = 0;

  private int resolveUrlSize = 0;

  private int afterDnsResolveIndex = 0;

  private BackOff backOff = new BackOff();

  public String getUrl() {
    if (isEmpty()) {
      throw new ServiceCombRuntimeException("no available address");
    }
    if (resolveUrlSize > 0) {
      return urlList.get(afterDnsResolveIndex);
    }
    return urlList.get(index);
  }

  public void addUrl(List<String> urls) {
    if (CollectionUtils.isEmpty(urls)) {
      return;
    }
    urlList.addAll(urls);
    index = urlList.size() == 0 ? 0 : new Random().nextInt(urlList.size());
  }


  public void addUrlAfterDnsResolve(List<String> urls) {
    if (CollectionUtils.isEmpty(urls)) {
      return;
    }
    List<String> availableUrls = urls.stream().filter(url -> {
      try (Socket s = new Socket()) {
        String[] ipPort = URLUtil.splitIpPort(url);
        s.connect(new InetSocketAddress(ipPort[0], Integer.parseInt(ipPort[1])), 3000);
      } catch (IOException e) {
        return false;
      }
      return true;
    }).collect(Collectors.toList());
    resolveUrlSize = availableUrls.size();
    afterDnsResolveIndex =
        urlList.size() + availableUrls.size() == 0 ? 0 : new Random().nextInt(availableUrls.size());
    urlList.addAll(availableUrls);
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
        backOff.backOff();
      }
    } else {
      index = (index + 1) % urlList.size();
      if (index == 0) {
        backOff.backOff();
      }
    }
    backOff.waiting();
  }
}
