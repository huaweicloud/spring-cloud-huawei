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

import org.apache.servicecomb.service.center.client.model.DataCenterInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.cloud.servicecomb.discovery")
public class DiscoveryBootstrapProperties {

  private boolean enabled = true;

  private boolean watch = false;

  private boolean enableZoneAware = false;

  private String address;

  private String appName = "default";

  @Value("${spring.cloud.servicecomb.discovery.serviceName:${spring.application.name:}}")
  private String serviceName;

  @Value("${server.env:}")
  private String environment;

  private String version;

  private String hostname;

  private boolean preferIpAddress;

  private boolean healthCheck = true;


  private int healthCheckInterval = 15000;

  private int healthCheckRequestTimeout = 5000;

  private int pollInterval = 15000;

  private int refreshInterval = 30000;

  private boolean autoDiscovery = false;

  @Value("${spring.cloud.servicecomb.discovery.allowCrossApp:false}")
  private boolean allowCrossApp;

  @Value("${server.publishAddress:}")
  private String serverAddress;

  @Value("${spring.cloud.servicecomb.discovery.ignoreSwaggerDifferent:false}")
  private boolean ignoreSwaggerDifferent;

  private DataCenterInfo datacenter;

  public String getServerAddress() {
    return serverAddress;
  }

  public void setServerAddress(String serverAddress) {
    this.serverAddress = serverAddress;
  }

  public DataCenterInfo getDatacenter() {
    return datacenter;
  }

  public void setDatacenter(DataCenterInfo datacenter) {
    this.datacenter = datacenter;
  }

  public String getAppName() {
    return appName;
  }

  public String getEnvironment() {
    return environment;
  }

  public void setEnvironment(String environment) {
    this.environment = environment;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public String getServiceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public boolean isEnableZoneAware() {
    return enableZoneAware;
  }

  public void setEnableZoneAware(boolean enableZoneAware) {
    this.enableZoneAware = enableZoneAware;
  }

  public String getHostname() {
    return hostname;
  }

  public void setHostname(String hostname) {
    this.hostname = hostname;
  }

  public boolean isPreferIpAddress() {
    return preferIpAddress;
  }

  public void setPreferIpAddress(boolean preferIpAddress) {
    this.preferIpAddress = preferIpAddress;
  }

  public boolean isHealthCheck() {
    return healthCheck;
  }

  public void setHealthCheck(boolean healthCheck) {
    this.healthCheck = healthCheck;
  }

  public int getHealthCheckInterval() {
    return healthCheckInterval;
  }

  public void setHealthCheckInterval(int healthCheckInterval) {
    this.healthCheckInterval = healthCheckInterval;
  }

  public int getHealthCheckRequestTimeout() {
    return healthCheckRequestTimeout;
  }

  public void setHealthCheckRequestTimeout(int healthCheckRequestTimeout) {
    this.healthCheckRequestTimeout = healthCheckRequestTimeout;
  }

  public int getPollInterval() {
    return pollInterval;
  }

  public void setPollInterval(int pollInterval) {
    this.pollInterval = pollInterval;
  }

  public int getRefreshInterval() {
    return refreshInterval;
  }

  public void setRefreshInterval(int refreshInterval) {
    this.refreshInterval = refreshInterval;
  }

  public boolean isAutoDiscovery() {
    return autoDiscovery;
  }

  public void setAutoDiscovery(boolean autoDiscovery) {
    this.autoDiscovery = autoDiscovery;
  }

  public boolean isAllowCrossApp() {
    return allowCrossApp;
  }

  public void setAllowCrossApp(boolean allowCrossApp) {
    this.allowCrossApp = allowCrossApp;
  }

  public boolean isIgnoreSwaggerDifferent() {
    return ignoreSwaggerDifferent;
  }

  public void setIgnoreSwaggerDifferent(boolean ignoreSwaggerDifferent) {
    this.ignoreSwaggerDifferent = ignoreSwaggerDifferent;
  }

  public boolean isWatch() {
    return watch;
  }

  public void setWatch(boolean watch) {
    this.watch = watch;
  }

  @Override
  public String toString() {
    return "ServiceCombDiscoveryProperties{" +
        "enabled=" + enabled +
        ", watch=" + watch +
        ", address='" + address + '\'' +
        ", appName='" + appName + '\'' +
        ", serviceName='" + serviceName + '\'' +
        ", environment='" + environment + '\'' +
        ", version='" + version + '\'' +
        ", hostname='" + hostname + '\'' +
        ", preferIpAddress=" + preferIpAddress +
        ", healthCheck=" + healthCheck +
        ", healthCheckInterval=" + healthCheckInterval +
        ", autoDiscovery=" + autoDiscovery +
        ", allowCrossApp=" + allowCrossApp +
        ", dataCenter=" + datacenter +
        '}';
  }
}
