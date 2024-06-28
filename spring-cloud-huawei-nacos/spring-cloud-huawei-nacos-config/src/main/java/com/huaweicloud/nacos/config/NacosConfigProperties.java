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

package com.huaweicloud.nacos.config;

import static com.alibaba.nacos.api.PropertyKeyConst.ACCESS_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.CLUSTER_NAME;
import static com.alibaba.nacos.api.PropertyKeyConst.CONFIG_LONG_POLL_TIMEOUT;
import static com.alibaba.nacos.api.PropertyKeyConst.CONFIG_RETRY_TIME;
import static com.alibaba.nacos.api.PropertyKeyConst.ENABLE_REMOTE_SYNC_CONFIG;
import static com.alibaba.nacos.api.PropertyKeyConst.ENCODE;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT;
import static com.alibaba.nacos.api.PropertyKeyConst.ENDPOINT_PORT;
import static com.alibaba.nacos.api.PropertyKeyConst.MAX_RETRY;
import static com.alibaba.nacos.api.PropertyKeyConst.NAMESPACE;
import static com.alibaba.nacos.api.PropertyKeyConst.PASSWORD;
import static com.alibaba.nacos.api.PropertyKeyConst.SECRET_KEY;
import static com.alibaba.nacos.api.PropertyKeyConst.SERVER_ADDR;
import static com.alibaba.nacos.api.PropertyKeyConst.USERNAME;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;
import java.util.Objects;
import java.util.Properties;

@ConfigurationProperties(NacosConfigProperties.PREFIX)
public class NacosConfigProperties {
  public static final String PREFIX = "spring.cloud.nacos.config";

  private boolean enabled = true;

  private String serverAddr = "http://127.0.0.1:8848";

  private String username;

  private String password;

  private String accessKey;

  private String secretKey;

  private String encode;

  private String group = "DEFAULT_GROUP";

  private String fileExtension = "properties";

  private int timeout = 3000;

  private String maxRetry;

  private String configLongPollTimeout;

  private String configRetryTime;

  private boolean enableRemoteSyncConfig = false;

  private String endpoint;

  private String namespace;

  private String clusterName;

  @Value("${spring.application.name}")
  private String name;

  private List<Config> sharedConfigs;

  private List<Config> extensionConfigs;

  /**
   * if set false, name/name + fileExtension/name + active + fileExtension dataId config will not dynamic refresh
   * and all config no longer listening for changes
   */
  private boolean refreshEnabled = true;

  private boolean masterStandbyEnabled = false;

  private String standbyServerAddr;

  private int order = 100;

  private long masterStandbyServerTaskDelay = 10000;

  private boolean retryMasterServerEnabled = true;

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getServerAddr() {
    return serverAddr;
  }

  public void setServerAddr(String serverAddr) {
    this.serverAddr = serverAddr;
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

  public String getEncode() {
    return encode;
  }

  public void setEncode(String encode) {
    this.encode = encode;
  }

  public String getGroup() {
    return group;
  }

  public void setGroup(String group) {
    this.group = group;
  }

  public String getFileExtension() {
    return fileExtension;
  }

  public void setFileExtension(String fileExtension) {
    this.fileExtension = fileExtension;
  }

  public int getTimeout() {
    return timeout;
  }

  public void setTimeout(int timeout) {
    this.timeout = timeout;
  }

  public String getMaxRetry() {
    return maxRetry;
  }

  public void setMaxRetry(String maxRetry) {
    this.maxRetry = maxRetry;
  }

  public String getConfigLongPollTimeout() {
    return configLongPollTimeout;
  }

  public void setConfigLongPollTimeout(String configLongPollTimeout) {
    this.configLongPollTimeout = configLongPollTimeout;
  }

  public String getConfigRetryTime() {
    return configRetryTime;
  }

  public void setConfigRetryTime(String configRetryTime) {
    this.configRetryTime = configRetryTime;
  }

  public boolean isEnableRemoteSyncConfig() {
    return enableRemoteSyncConfig;
  }

  public void setEnableRemoteSyncConfig(boolean enableRemoteSyncConfig) {
    this.enableRemoteSyncConfig = enableRemoteSyncConfig;
  }

  public String getEndpoint() {
    return endpoint;
  }

  public void setEndpoint(String endpoint) {
    this.endpoint = endpoint;
  }

  public String getNamespace() {
    return namespace;
  }

  public void setNamespace(String namespace) {
    this.namespace = namespace;
  }

  public String getClusterName() {
    return clusterName;
  }

  public void setClusterName(String clusterName) {
    this.clusterName = clusterName;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public List<Config> getSharedConfigs() {
    return sharedConfigs;
  }

  public void setSharedConfigs(List<Config> sharedConfigs) {
    this.sharedConfigs = sharedConfigs;
  }

  public List<Config> getExtensionConfigs() {
    return extensionConfigs;
  }

  public void setExtensionConfigs(List<Config> extensionConfigs) {
    this.extensionConfigs = extensionConfigs;
  }

  public boolean isRefreshEnabled() {
    return refreshEnabled;
  }

  public void setRefreshEnabled(boolean refreshEnabled) {
    this.refreshEnabled = refreshEnabled;
  }

  public boolean isMasterStandbyEnabled() {
    return masterStandbyEnabled;
  }

  public void setMasterStandbyEnabled(boolean masterStandbyEnabled) {
    this.masterStandbyEnabled = masterStandbyEnabled;
  }

  public String getStandbyServerAddr() {
    return standbyServerAddr;
  }

  public void setStandbyServerAddr(String standbyServerAddr) {
    this.standbyServerAddr = standbyServerAddr;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  public long getMasterStandbyServerTaskDelay() {
    return masterStandbyServerTaskDelay;
  }

  public void setMasterStandbyServerTaskDelay(long masterStandbyServerTaskDelay) {
    this.masterStandbyServerTaskDelay = masterStandbyServerTaskDelay;
  }

  public boolean isRetryMasterServerEnabled() {
    return retryMasterServerEnabled;
  }

  public void setRetryMasterServerEnabled(boolean retryMasterServerEnabled) {
    this.retryMasterServerEnabled = retryMasterServerEnabled;
  }

  public Properties assembleMasterNacosServerProperties() {
    return buildProperties(Objects.toString(this.serverAddr, ""));
  }

  public Properties assembleStandbyNacosServerProperties() {
    return buildProperties(Objects.toString(this.standbyServerAddr, ""));
  }

  private Properties buildProperties(String serverAddress) {
    Properties properties = new Properties();
    properties.put(SERVER_ADDR, serverAddress);
    properties.put(USERNAME, Objects.toString(this.username, ""));
    properties.put(PASSWORD, Objects.toString(this.password, ""));
    properties.put(ENCODE, Objects.toString(this.encode, ""));
    properties.put(NAMESPACE, Objects.toString(this.namespace, ""));
    properties.put(ACCESS_KEY, Objects.toString(this.accessKey, ""));
    properties.put(SECRET_KEY, Objects.toString(this.secretKey, ""));
    properties.put(CLUSTER_NAME, Objects.toString(this.clusterName, ""));
    properties.put(MAX_RETRY, Objects.toString(this.maxRetry, ""));
    properties.put(CONFIG_LONG_POLL_TIMEOUT, Objects.toString(this.configLongPollTimeout, ""));
    properties.put(CONFIG_RETRY_TIME, Objects.toString(this.configRetryTime, ""));
    properties.put(ENABLE_REMOTE_SYNC_CONFIG, Objects.toString(this.enableRemoteSyncConfig, ""));
    String endpoint = Objects.toString(this.endpoint, "");
    if (endpoint.contains(":")) {
      int index = endpoint.indexOf(":");
      properties.put(ENDPOINT, endpoint.substring(0, index));
      properties.put(ENDPOINT_PORT, endpoint.substring(index + 1));
    }
    else {
      properties.put(ENDPOINT, endpoint);
    }
    return properties;
  }

  public static class Config {
    private String dataId;

    private String group = "DEFAULT_GROUP";

    private boolean refresh = false;

    public String getDataId() {
      return dataId;
    }

    public void setDataId(String dataId) {
      this.dataId = dataId;
    }

    public String getGroup() {
      return group;
    }

    public void setGroup(String group) {
      this.group = group;
    }

    public boolean isRefresh() {
      return refresh;
    }

    public void setRefresh(boolean refresh) {
      this.refresh = refresh;
    }
  }
}
