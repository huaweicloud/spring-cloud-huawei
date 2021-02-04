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

import com.huaweicloud.common.transport.URLConfig;
import com.huaweicloud.common.util.URLUtil;
import com.huaweicloud.config.ServiceCombConfigProperties;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;

import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.transport.HttpTransport;

/**
 * @Author wangqijun
 * @Date 11:09 2019-10-17
 **/
public abstract class ServiceCombConfigClient {

  protected HttpTransport httpTransport;

  protected URLConfig configCenterConfig = new URLConfig();

  protected List<String> fileSources = new ArrayList<>();

  protected String revision = "0";

  public ServiceCombConfigClient(String urls, HttpTransport httpTransport, String fileSource) {
    this.httpTransport = httpTransport;
    configCenterConfig.addUrl(URLUtil.getEnvConfigUrl());
    if (configCenterConfig.isEmpty()) {
      configCenterConfig.addUrl(URLUtil.dealMultiUrl(urls));
    }
    if (StringUtils.isNotEmpty(fileSource)) {
      fileSources = Arrays.asList(fileSource.split(","));
    }
  }

  protected void filterConfig(Map<String, Object> rawConfig, Map<String, Object> kvConfig,
      Map<String, Object> fileConfig) {
    for (Entry<String, Object> entry : rawConfig.entrySet()) {
      boolean isKV = true;
      for (String source : fileSources) {
        if (entry.getKey().equals(source)) {
          fileConfig.put(entry.getKey(), entry.getValue());
          isKV = false;
          break;
        }
      }
      if (isKV) {
        kvConfig.put(entry.getKey(), entry.getValue());
      }
    }
  }
  /**
   * load all remote config from config center
   *
   * @return
   * @throws RemoteOperationException
   */
  public abstract Map<String, Object> loadAll(
      ServiceCombConfigProperties serviceCombConfigProperties, String project)
      throws RemoteOperationException;
}
