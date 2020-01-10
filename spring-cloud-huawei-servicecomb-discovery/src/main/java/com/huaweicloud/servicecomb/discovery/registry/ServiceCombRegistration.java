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

package com.huaweicloud.servicecomb.discovery.registry;

import java.net.URI;
import java.util.Collections;
import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;
import com.huaweicloud.common.util.NetUtil;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

public class ServiceCombRegistration implements Registration, ServiceInstance {

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  public ServiceCombRegistration(ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
  }


  @Override
  public String getServiceId() {
    return serviceCombDiscoveryProperties.getServiceName();
  }

  @Override
  public String getHost() {
    return NetUtil.getHost(serviceCombDiscoveryProperties.getAddress());
  }

  @Override
  public int getPort() {
    return NetUtil.getPort(serviceCombDiscoveryProperties.getAddress());
  }

  @Override
  public boolean isSecure() {
    return false;
  }

  @Override
  public URI getUri() {
    return URI.create(serviceCombDiscoveryProperties.getAddress());
  }

  @Override//TODO
  public Map<String, String> getMetadata() {
    return Collections.emptyMap();
  }

  public ServiceCombDiscoveryProperties getServiceCombDiscoveryProperties() {
    return serviceCombDiscoveryProperties;
  }

  public String getAppName() {
    return serviceCombDiscoveryProperties.getAppName();
  }

  public String getVersion() {
    return serviceCombDiscoveryProperties.getVersion();
  }

  public String getEnvironment() {
    return serviceCombDiscoveryProperties.getEnvironment();
  }
}
