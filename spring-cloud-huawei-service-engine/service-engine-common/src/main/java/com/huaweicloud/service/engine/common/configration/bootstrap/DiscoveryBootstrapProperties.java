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

package com.huaweicloud.service.engine.common.configration.bootstrap;


import org.apache.servicecomb.service.center.client.model.DataCenterInfo;
import org.springframework.beans.factory.annotation.Value;

public class DiscoveryBootstrapProperties {
  private static final int HEALTH_CHECK_MAX_INTERVAL = 600;

  private static final int HEALTH_CHECK_MIN_INTERVAL = 1;

  private boolean enabled = true;

  private int order = 100;

  private boolean watch = false;

  private boolean enableZoneAware = false;

  private String address;

  private String hostname;

  private boolean preferIpAddress;

  private boolean healthCheck = true;

  private int healthCheckInterval = 15;

  private int healthCheckRequestTimeout = 5000;

  private int pollInterval = 15000;

  private boolean autoDiscovery = false;

  private int waitTimeForShutDownInMillis = 15000;

  private boolean allowCrossApp;

  @Value("${spring.cloud.servicecomb.discovery.publishAddress:${server.publishAddress:}}")
  private String publishAddress;

  private boolean ignoreSwaggerDifferent;

  private boolean canOverwriteSwagger = true;

  private DataCenterInfo datacenter;

  // when service polling is enabled, client will try to query service names
  // to check if service names list changed. This is quite useful when in
  // spring cloud gateway and
  // spring:
  //  cloud:
  //    gateway:
  //      discovery:
  //        locator:
  //          enabled: true
  private boolean enableServicePolling = false;

  public String getPublishAddress() {
    return publishAddress;
  }

  public void setPublishAddress(String publishAddress) {
    this.publishAddress = publishAddress;
  }

  public DataCenterInfo getDatacenter() {
    return datacenter;
  }

  public void setDatacenter(DataCenterInfo datacenter) {
    this.datacenter = datacenter;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
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
    if (healthCheckInterval <= HEALTH_CHECK_MAX_INTERVAL && healthCheckInterval >= HEALTH_CHECK_MIN_INTERVAL) {
      this.healthCheckInterval = healthCheckInterval;
    }
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

  public boolean isEnableServicePolling() {
    return enableServicePolling;
  }

  public void setEnableServicePolling(boolean enableServicePolling) {
    this.enableServicePolling = enableServicePolling;
  }

  public boolean isWatch() {
    return watch;
  }

  public void setWatch(boolean watch) {
    this.watch = watch;
  }

  public int getWaitTimeForShutDownInMillis() {
    return waitTimeForShutDownInMillis;
  }

  public void setWaitTimeForShutDownInMillis(int waitTimeForShutDownInMillis) {
    this.waitTimeForShutDownInMillis = waitTimeForShutDownInMillis;
  }

  public boolean isCanOverwriteSwagger() {
    return canOverwriteSwagger;
  }

  public void setCanOverwriteSwagger(boolean canOverwriteSwagger) {
    this.canOverwriteSwagger = canOverwriteSwagger;
  }

  public int getOrder() {
    return order;
  }

  public void setOrder(int order) {
    this.order = order;
  }

  @Override
  public String toString() {
    return "ServiceCombDiscoveryProperties{" +
        "enabled=" + enabled +
        ", watch=" + watch +
        ", address='" + address + '\'' +
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
