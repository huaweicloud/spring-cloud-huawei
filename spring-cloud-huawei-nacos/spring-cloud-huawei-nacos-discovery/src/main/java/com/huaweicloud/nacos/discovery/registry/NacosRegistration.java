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

package com.huaweicloud.nacos.discovery.registry;

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NacosRegistration implements Registration {
  private final List<NacosRegistrationMetadataCustomizer> registrationCustomizers;

  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  private String instanceId;

  public NacosRegistration(List<NacosRegistrationMetadataCustomizer> registrationCustomizers,
      NacosDiscoveryProperties nacosDiscoveryProperties) {
    this.registrationCustomizers = registrationCustomizers;
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
    init();
  }

  public void init() {
    customize();
    this.instanceId = buildInstanceId();
  }

  protected void customize() {
    if (registrationCustomizers != null) {
      for (NacosRegistrationMetadataCustomizer customizer : registrationCustomizers) {
        customizer.customize(this);
      }
    }
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String getServiceId() {
    return nacosDiscoveryProperties.getService();
  }

  @Override
  public String getHost() {
    return nacosDiscoveryProperties.getIp();
  }

  @Override
  public int getPort() {
    return nacosDiscoveryProperties.getPort();
  }

  @Override
  public boolean isSecure() {
    return nacosDiscoveryProperties.isSecure();
  }

  @Override
  public URI getUri() {
    return DefaultServiceInstance.getUri(this);
  }

  @Override
  public Map<String, String> getMetadata() {
    return nacosDiscoveryProperties.getMetadata();
  }

  public NacosDiscoveryProperties getNacosDiscoveryProperties() {
    return nacosDiscoveryProperties;
  }

  private String buildInstanceId() {
    String result = nacosDiscoveryProperties.getIp() + ":" + nacosDiscoveryProperties.getPort();
    return result.replaceAll("[^0-9a-zA-Z]", "-");
  }
}
