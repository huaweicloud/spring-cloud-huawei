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

package com.huaweicloud.zookeeper.discovery;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

public class ZookeeperDiscoveryProperties {
  private boolean enabled = true;

  private Map<String, String> metadata = new HashMap<>();

  private String instanceStatus = ZookeeperConstants.STATUS_UP;

  private boolean registerEnabled = true;

  @Value("${spring.cloud.zookeeper.discovery.serviceName:${spring.application.name:}}")
  private String serviceName;

  private String root = "/services";

  private String ip;

  @Value("${spring.cloud.zookeeper.discovery.port:${server.port:}}")
  private int port = -1;

  private boolean secure = false;

  private String ipType;

  /**
   * set alias path for service discovery, support discovery children service
   */
  private Map<String, String> discoveryPathForAlias = new HashMap<>();

  /**
   * heartBeat delay time. Time unit: millisecond.
   */
  private long heartBeatTaskDelay = 30000;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public String getInstanceStatus() {
    return instanceStatus;
  }

  public void setInstanceStatus(String instanceStatus) {
    this.instanceStatus = instanceStatus;
  }

  public boolean isRegisterEnabled() {
    return registerEnabled;
  }

  public void setRegisterEnabled(boolean registerEnabled) {
    this.registerEnabled = registerEnabled;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getRoot() {
    return root;
  }

  public void setRoot(String root) {
    this.root = root;
  }

  public String getIp() {
    return ip;
  }

  public void setIp(String ip) {
    this.ip = ip;
  }

  public int getPort() {
    return port;
  }

  public void setPort(int port) {
    this.port = port;
  }

  public boolean isSecure() {
    return secure;
  }

  public void setSecure(boolean secure) {
    this.secure = secure;
  }

  public String getIpType() {
    return ipType;
  }

  public void setIpType(String ipType) {
    this.ipType = ipType;
  }

  public Map<String, String> getDiscoveryPathForAlias() {
    return discoveryPathForAlias;
  }

  public void setDiscoveryPathForAlias(Map<String, String> discoveryPathForAlias) {
    this.discoveryPathForAlias = discoveryPathForAlias;
  }

  public long getHeartBeatTaskDelay() {
    return heartBeatTaskDelay;
  }

  public void setHeartBeatTaskDelay(long heartBeatTaskDelay) {
    this.heartBeatTaskDelay = heartBeatTaskDelay;
  }
}
