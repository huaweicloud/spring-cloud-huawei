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

package com.huaweicloud.config;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.huaweicloud.common.exception.RemoteOperationException;
import com.huaweicloud.config.client.QueryParamUtil;
import com.huaweicloud.config.client.ServiceCombConfigClient;
import org.springframework.core.env.EnumerablePropertySource;
import org.springframework.retry.annotation.Retryable;

/**
 * @Author wangqijun
 * @Date 12:44 2019-10-19
 **/
public class ServiceCombConfigPropertySource extends EnumerablePropertySource<ServiceCombConfigClient> {

  private final Map<String, Object> properties = new LinkedHashMap<>();

  private ServiceCombConfigClient serviceCombConfigClient;


  public ServiceCombConfigPropertySource(String name,
      ServiceCombConfigClient source) {
    super(name, source);
    this.serviceCombConfigClient = source;
  }

  @Retryable(interceptor = "serviceCombConfigRetryInterceptor")
  public Map<String, String> loadAllRemoteConfig(ServiceCombConfigProperties serviceCombConfigProperties,
      String project)
      throws RemoteOperationException {
    Map<String, String> remoteConfig = serviceCombConfigClient
        .loadAll(serviceCombConfigProperties, project);
    if (remoteConfig == null) {
      return Collections.emptyMap();
    }
    properties.putAll(remoteConfig);
    return remoteConfig;
  }

  @Override
  public String[] getPropertyNames() {
    Set<String> strings = this.properties.keySet();
    return strings.toArray(new String[strings.size()]);
  }

  @Override
  public Object getProperty(String name) {
    return properties.get(name);
  }
}
