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
import java.util.Map;
import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.common.transport.HttpTransport;

/**
 * @Author wangqijun
 * @Date 11:09 2019-10-17
 **/
public abstract class ServiceCombConfigClient {

  protected HttpTransport httpTransport;

  protected URLConfig configCenterConfig = new URLConfig();

  protected String revision = "0";

  public ServiceCombConfigClient(String urls, HttpTransport httpTransport) {
    this.httpTransport = httpTransport;
    configCenterConfig.addUrl(URLUtil.getEnvConfigUrl());
    if (configCenterConfig.isEmpty()) {
      configCenterConfig.addUrl(URLUtil.dealMultiUrl(urls));
    }
  }

  /**
   * load all remote config from config center
   *
   * @return
   * @throws RemoteOperationException
   */
  public abstract Map<String, String> loadAll(
      ServiceCombConfigProperties serviceCombConfigProperties, String project)
      throws RemoteOperationException;
}
