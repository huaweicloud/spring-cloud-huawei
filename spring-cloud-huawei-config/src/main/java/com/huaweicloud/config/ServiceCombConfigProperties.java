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

package com.huaweicloud.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import com.huaweicloud.common.exception.ServiceCombRuntimeException;

@ConfigurationProperties("spring.cloud.servicecomb.config")
public class ServiceCombConfigProperties {

  private boolean enabled = true;

  private boolean firstPullRequired = true;

  @Value("${spring.cloud.servicecomb.discovery.address:}")
  private String discoveryAddress;

  @Value("${spring.cloud.servicecomb.discovery.serviceName:${spring.application.name:}}")
  private String serviceName;

  @Value("${spring.cloud.servicecomb.discovery.appName:default}")
  private String appName;

  @Value("${spring.cloud.servicecomb.discovery.version:}")
  private String version;

  @Value("${server.env:}")
  private String env;

  @Value("${spring.cloud.servicecomb.config.serverType:}")
  private String serverType;

  private String serverAddr;

  @Value("${spring.cloud.servicecomb.config.fileSource:}")
  private String fileSource;

  private Watch watch = new Watch();

  private Kie kie = new Kie();

  public String getFileSource() {
    return fileSource;
  }

  public void setFileSource(String fileSource) {
    this.fileSource = fileSource;
  }

  public String getDiscoveryAddress() {
    return discoveryAddress;
  }

  public void setDiscoveryAddress(String discoveryAddress) {
    this.discoveryAddress = discoveryAddress;
  }

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

  public Watch getWatch() {
    return watch;
  }

  public void setWatch(Watch watch) {
    this.watch = watch;
  }

  public void setKie(Kie kie) {
    this.kie = kie;
  }

  public Kie getKie() {
    return this.kie;
  }

  public String getServiceName() {
    if (StringUtils.isEmpty(serviceName)) {
      throw new ServiceCombRuntimeException("please use bootstrap.yml for config properties.");
    }
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String getAppName() {
    return appName;
  }

  public void setAppName(String appName) {
    this.appName = appName;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getEnv() {
    return env;
  }

  public void setEnv(String env) {
    this.env = env;
  }

  public String getServerType() {
    return serverType;
  }

  public void setServerType(String serverType) {
    this.serverType = serverType;
  }

  public boolean isFirstPullRequired() {
    return firstPullRequired;
  }

  public void setFirstPullRequired(boolean firstPullRequired) {
    this.firstPullRequired = firstPullRequired;
  }

  public static class Watch {
    private boolean enable;

    private int delay = 10 * 1000;

    private int waitTime = 10 * 1000;

    public boolean isEnable() {
      return enable;
    }

    public void setEnable(boolean enable) {
      this.enable = enable;
    }

    public int getDelay() {
      return delay;
    }

    public void setDelay(int delay) {
      this.delay = delay;
    }

    public int getWaitTime() {
      return waitTime;
    }

    public void setWaitTime(int waitTime) {
      this.waitTime = waitTime;
    }
  }

  public static class Kie {
    private boolean enableAppConfig = true;

    private boolean enableServiceConfig = true;

    private boolean enableCustomConfig = true;

    private String customLabelValue = "";

    private String customLabel = "public";

    private boolean enableLongPolling = true;

    private int pollingWaitTimeInSeconds = 10;

    public int getPollingWaitTimeInSeconds() {
      return pollingWaitTimeInSeconds;
    }

    public void setPollingWaitTimeInSeconds(int pollingWaitTimeInSeconds) {
      this.pollingWaitTimeInSeconds = pollingWaitTimeInSeconds;
    }

    public boolean isEnableLongPolling() {
      return enableLongPolling;
    }

    public void setEnableLongPolling(boolean enableLongPolling) {
      this.enableLongPolling = enableLongPolling;
    }

    public boolean isEnableAppConfig() {
      return enableAppConfig;
    }

    public void setEnableAppConfig(boolean enableAppConfig) {
      this.enableAppConfig = enableAppConfig;
    }

    public boolean isEnableServiceConfig() {
      return enableServiceConfig;
    }

    public void setEnableServiceConfig(boolean enableServiceConfig) {
      this.enableServiceConfig = enableServiceConfig;
    }

    public boolean isEnableCustomConfig() {
      return enableCustomConfig;
    }

    public void setEnableCustomConfig(boolean enableCustomConfig) {
      this.enableCustomConfig = enableCustomConfig;
    }

    public String getCustomLabelValue() {
      return customLabelValue;
    }

    public void setCustomLabelValue(String customLabelValue) {
      this.customLabelValue = customLabelValue;
    }

    public String getCustomLabel() {
      return customLabel;
    }

    public void setCustomLabel(String customLabel) {
      this.customLabel = customLabel;
    }
  }
}
