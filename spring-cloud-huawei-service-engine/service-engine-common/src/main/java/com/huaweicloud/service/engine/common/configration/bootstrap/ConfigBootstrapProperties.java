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

public class ConfigBootstrapProperties {

  private boolean enabled = true;

  private boolean firstPullRequired = true;

  private String serverType;

  private String serverAddr;

  private String fileSource;

  private ConfigCenter configCenter = new ConfigCenter();

  private Kie kie = new Kie();

  public String getFileSource() {
    return fileSource;
  }

  public void setFileSource(String fileSource) {
    this.fileSource = fileSource;
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

  public void setKie(Kie kie) {
    this.kie = kie;
  }

  public Kie getKie() {
    return this.kie;
  }

  public ConfigCenter getConfigCenter() {
    return configCenter;
  }

  public void setConfigCenter(
      ConfigCenter configCenter) {
    this.configCenter = configCenter;
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

  public static class ConfigCenter {
    private long refreshInterval = 15000;

    public long getRefreshInterval() {
      return refreshInterval;
    }

    public void setRefreshInterval(long refreshInterval) {
      this.refreshInterval = refreshInterval;
    }
  }

  public static class Kie {
    private boolean enableAppConfig = true;

    private boolean enableServiceConfig = true;

    private boolean enableCustomConfig = true;

    private boolean enableVersionConfig = true;

    private String customLabelValue = "";

    private String customLabel = "public";

    private boolean enableLongPolling = true;

    private int pollingWaitTimeInSeconds = 10;

    private int refreshIntervalInMillis = 15000;

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

    public boolean isEnableVersionConfig() {
      return enableVersionConfig;
    }

    public void setEnableVersionConfig(boolean enableVersionConfig) {
      this.enableVersionConfig = enableVersionConfig;
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

    public int getRefreshIntervalInMillis() {
      return refreshIntervalInMillis;
    }

    public void setRefreshIntervalInMillis(int refreshIntervalInMillis) {
      this.refreshIntervalInMillis = refreshIntervalInMillis;
    }
  }
}
