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

package com.huaweicloud.servicecomb.discovery.discovery;

import com.huaweicloud.servicecomb.discovery.client.model.DataCenter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
import org.springframework.stereotype.Component;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

@Component
@ConfigurationProperties("spring.cloud.servicecomb.discovery")
public class ServiceCombDiscoveryProperties {

  private boolean enabled = true;

  private String address;

  private String appName;

  @Value("${spring.cloud.servicecomb.discovery.serviceName:${spring.application.name}}")
  private String serviceName;

  @Value("${server.env:}")
  private String environment;

  private String version;

  private String hostname;

  private boolean preferIpAddress;

  private boolean healthCheck = true;

  private int healthCheckInterval;

  private boolean autoDiscovery = false;

  private boolean allowCrossApp = false;

  @Value("${server.port}")
  private String port;

  private DataCenter datacenter;

  public DataCenter getDatacenter() {
    return datacenter;
  }

  public void setDatacenter(DataCenter datacenter) {
    this.datacenter = datacenter;
  }

  public String getAppName() {
    if (null == appName) {
      return ServiceRegistryConfig.DEFAULT_APPID;
    }
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
    if (healthCheckInterval == 0) {
      healthCheckInterval = ServiceRegistryConfig.DEFAULT_HEALTHCHECK_INTERVAL;
    }
    return healthCheckInterval;
  }

  public void setHealthCheckInterval(int healthCheckInterval) {
    this.healthCheckInterval = healthCheckInterval;
  }

  public boolean isAutoDiscovery() {
    return autoDiscovery;
  }

  public void setAutoDiscovery(boolean autoDiscovery) {
    this.autoDiscovery = autoDiscovery;
  }

  public String getPort() {
    return port;
  }

  public void setPort(String port) {
    this.port = port;
  }

  public boolean isAllowCrossApp() {
    return allowCrossApp;
  }

  public void setAllowCrossApp(boolean allowCrossApp) {
    this.allowCrossApp = allowCrossApp;
  }

  @Override
  public String toString() {
    return "ServiceCombDiscoveryProperties{" +
        "enabled=" + enabled +
        ", address='" + address + '\'' +
        ", appName='" + appName + '\'' +
        ", serviceName='" + serviceName + '\'' +
        ", version='" + version + '\'' +
        ", hostname='" + hostname + '\'' +
        ", preferIpAddress=" + preferIpAddress +
        ", healthCheck=" + healthCheck +
        ", healthCheckInterval='" + healthCheckInterval + '\'' +
        '}';
  }
}
