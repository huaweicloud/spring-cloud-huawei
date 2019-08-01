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

package org.springframework.cloud.servicecomb.discovery.discovery;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.servicecomb.discovery.client.model.ServiceRegistryConfig;
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

  private String serviceName;

  private String version;

  private String hostname;

  private boolean preferIpAddress;

  private boolean healthCheck;

  private String healthCheckInterval;

  public String getAppName() {
    if (null == appName) {
      return ServiceRegistryConfig.DEFAULT_APPID;
    }
    return appName;
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

  public String getHealthCheckInterval() {
    return healthCheckInterval;
  }

  public void setHealthCheckInterval(String healthCheckInterval) {
    this.healthCheckInterval = healthCheckInterval;
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
