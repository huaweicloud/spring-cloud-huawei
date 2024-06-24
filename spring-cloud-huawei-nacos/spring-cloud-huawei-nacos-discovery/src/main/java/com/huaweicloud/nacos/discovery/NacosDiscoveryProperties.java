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

package com.huaweicloud.nacos.discovery;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;

public class NacosDiscoveryProperties {
  private boolean enabled = true;

  private String serverAddr = "http://127.0.0.1:8848";

  private Map<String, String> metadata = new HashMap<>();

  private boolean ephemeral = true;

  private String username;

  private String password;

  private String accessKey;

  private String secretKey;

  private String namingLoadCacheAtStart = "false";

  private String clusterName = "DEFAULT";

  private float weight = 1;

  private boolean instanceEnabled = true;

  private String logName = "";

  private String namespace = "";

  private boolean registerEnabled = true;

  @Value("${spring.cloud.nacos.discovery.service:${spring.application.name:}}")
  private String service;

  private String group = "DEFAULT_GROUP";

  private String ip;

  @Value("${spring.cloud.nacos.discovery.port:${server.port:}}")
  private int port = -1;

  private boolean secure = false;

  /**
   * the time interval for instance send heartBeat to nacos server. Time unit: millisecond.
   */
  private Integer heartBeatInterval;

  /**
   * the timeout for instance send heartBeat to nacos server. Time unit: millisecond.
   */
  private Integer heartBeatTimeout;

  /**
   * the timeout for nacos server delete instance ip. Time unit: millisecond.
   */
  private Integer ipDeleteTimeout;

  private String ipType;

  private String networkInterface;

  private long heartBeatTaskDelay = 30000;

  public String getServerAddr() {
    return serverAddr;
  }

  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
  }

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

  public boolean isEphemeral() {
    return ephemeral;
  }

  public void setEphemeral(boolean ephemeral) {
    this.ephemeral = ephemeral;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getPassword() {
    return password;
  }

  public void setPassword(String password) {
    this.password = password;
  }

  public String getAccessKey() {
    return accessKey;
  }

  public void setAccessKey(String accessKey) {
    this.accessKey = accessKey;
  }

  public String getSecretKey() {
    return secretKey;
  }

  public void setSecretKey(String secretKey) {
    this.secretKey = secretKey;
  }

  public String getNamingLoadCacheAtStart() {
    return namingLoadCacheAtStart;
  }

  public void setNamingLoadCacheAtStart(String namingLoadCacheAtStart) {
    this.namingLoadCacheAtStart = namingLoadCacheAtStart;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public float getWeight() {
    return weight;
  }

  public void setWeight(float weight) {
    this.weight = weight;
  }

  public boolean isInstanceEnabled() {
    return instanceEnabled;
  }

  public void setInstanceEnabled(boolean instanceEnabled) {
    this.instanceEnabled = instanceEnabled;
  }

  public String getLogName() {
    return logName;
  }

  public void setLogName(String logName) {
    this.logName = logName;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public boolean isRegisterEnabled() {
    return registerEnabled;
  }

  public void setRegisterEnabled(boolean registerEnabled) {
    this.registerEnabled = registerEnabled;
  }

  public String getService() {
    return service;
  }

  public void setService(String service) {
    this.service = service;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
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

  public Integer getHeartBeatInterval() {
    return heartBeatInterval;
  }

  public void setHeartBeatInterval(Integer heartBeatInterval) {
    this.heartBeatInterval = heartBeatInterval;
  }

  public Integer getHeartBeatTimeout() {
    return heartBeatTimeout;
  }

  public void setHeartBeatTimeout(Integer heartBeatTimeout) {
    this.heartBeatTimeout = heartBeatTimeout;
  }

  public Integer getIpDeleteTimeout() {
    return ipDeleteTimeout;
  }

  public void setIpDeleteTimeout(Integer ipDeleteTimeout) {
    this.ipDeleteTimeout = ipDeleteTimeout;
  }

  public String getIpType() {
    return ipType;
  }

  public void setIpType(String ipType) {
    this.ipType = ipType;
  }

  public String getNetworkInterface() {
    return networkInterface;
  }

  public void setNetworkInterface(String networkInterface) {
    this.networkInterface = networkInterface;
  }

  public long getHeartBeatTaskDelay() {
    return heartBeatTaskDelay;
  }

  public void setHeartBeatTaskDelay(long heartBeatTaskDelay) {
    this.heartBeatTaskDelay = heartBeatTaskDelay;
  }
}
