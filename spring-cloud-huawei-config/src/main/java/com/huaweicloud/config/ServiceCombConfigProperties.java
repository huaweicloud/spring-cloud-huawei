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

import com.huaweicloud.common.util.URLUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import com.huaweicloud.common.exception.ServiceCombRuntimeException;

/**
 * @Author wangqijun
 * @Date 11:05 2019-10-17
 **/
@Component
@ConfigurationProperties("spring.cloud.servicecomb.config")
public class ServiceCombConfigProperties {

  private boolean enabled = true;

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

  @Value("${spring.cloud.servicecomb.config.enableLongPolling:true}")
  private boolean enableLongPolling;

  @Value("${spring.cloud.servicecomb.config.fileSource:}")
  private String fileSource;

  private Watch watch = new Watch();

  public String getFileSource() {
    return fileSource;
  }

  public void setFileSource(String fileSource) {
    this.fileSource = fileSource;
  }

  public String getDiscoveryAddress() {
    String url = org.apache.commons.lang3.StringUtils.join(URLUtil.getEnvServerURL(), ",");
    return url.isEmpty() ? discoveryAddress : url;
  }

  public void setDiscoveryAddress(String discoveryAddress) {
    this.discoveryAddress = discoveryAddress;
  }

  public boolean isEnableLongPolling() {
    return enableLongPolling;
  }

  public boolean getEnableLongPolling() {
    return enableLongPolling;
  }

  public void setEnableLongPolling(boolean enableLongPolling) {
    this.enableLongPolling = enableLongPolling;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getServerAddr() {
    String url = org.apache.commons.lang3.StringUtils.join(URLUtil.getEnvConfigUrl(), ",");
    return url.isEmpty() ? serverAddr : url;
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

  public static class Watch {
    private boolean enable;

    private int delay = 10 * 1000;

    private int waitTime = 10 * 1000;

    @Value("${spring.cloud.servicecomb.config.pollingWaitSec:30}")
    private int pollingWaitTimeInSeconds = 30;

    public int getPollingWaitTimeInSeconds() {
      return pollingWaitTimeInSeconds;
    }

    public void setPollingWaitTimeInSeconds(int pollingWaitTimeInSeconds) {
      this.pollingWaitTimeInSeconds = pollingWaitTimeInSeconds;
    }

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
}
