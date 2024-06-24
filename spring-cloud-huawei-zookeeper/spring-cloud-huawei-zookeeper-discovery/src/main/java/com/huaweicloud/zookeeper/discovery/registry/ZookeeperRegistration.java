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

package com.huaweicloud.zookeeper.discovery.registry;

import java.net.URI;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.foundation.common.net.NetUtils;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.huaweicloud.zookeeper.discovery.ZookeeperDiscoveryProperties;

public class ZookeeperRegistration implements Registration {
  private static final String IPV6 = "IPv6";

  private static final String REGISTER_SOURCE = "register.source";

  private final ZookeeperDiscoveryProperties zookeeperDiscoveryProperties;

  private String instanceId;

  public ZookeeperRegistration(ZookeeperDiscoveryProperties zookeeperDiscoveryProperties) {
    this.zookeeperDiscoveryProperties = zookeeperDiscoveryProperties;
    init();
  }

  public void init() {
    buildPropertiesAttributes();
    this.instanceId = buildInstanceId();
  }

  private void buildPropertiesAttributes() {
    Map<String, String> metadata = zookeeperDiscoveryProperties.getMetadata();
    metadata.put(REGISTER_SOURCE, "SPRING_CLOUD");
    if (zookeeperDiscoveryProperties.isSecure()) {
      metadata.put("secure", "true");
    }
    if (StringUtils.isEmpty(zookeeperDiscoveryProperties.getIp())) {
      String ip = IPV6.equalsIgnoreCase(zookeeperDiscoveryProperties.getIpType())
          && !StringUtils.isEmpty(NetUtils.getIpv6HostAddress())
          ? NetUtils.getIpv6HostAddress() : NetUtils.getHostAddress();
      zookeeperDiscoveryProperties.setIp(ip);
    }
  }

  @Override
  public String getInstanceId() {
    return instanceId;
  }

  @Override
  public String getServiceId() {
    return zookeeperDiscoveryProperties.getServiceName();
  }

  @Override
  public String getHost() {
    return zookeeperDiscoveryProperties.getIp();
  }

  @Override
  public int getPort() {
    return zookeeperDiscoveryProperties.getPort();
  }

  @Override
  public boolean isSecure() {
    return zookeeperDiscoveryProperties.isSecure();
  }

  @Override
  public URI getUri() {
    return DefaultServiceInstance.getUri(this);
  }

  @Override
  public Map<String, String> getMetadata() {
    return zookeeperDiscoveryProperties.getMetadata();
  }

  public ZookeeperDiscoveryProperties getzookeeperDiscoveryProperties() {
    return zookeeperDiscoveryProperties;
  }

  private String buildInstanceId() {
    String result = zookeeperDiscoveryProperties.getIp() + ":" + zookeeperDiscoveryProperties.getPort();
    return result.replaceAll("[^0-9a-zA-Z]", "-");
  }
}
