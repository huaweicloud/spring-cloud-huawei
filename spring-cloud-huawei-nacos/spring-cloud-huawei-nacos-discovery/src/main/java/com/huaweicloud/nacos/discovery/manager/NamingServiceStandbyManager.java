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

package com.huaweicloud.nacos.discovery.manager;

import java.time.Duration;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.huaweicloud.nacos.discovery.NacosConst;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NamingServiceStandbyManager implements NamingServiceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(NamingServiceStandbyManager.class);

  private final NacosDiscoveryProperties properties;

  private volatile NamingService namingService;

  private volatile NamingMaintainService namingMaintainService;

  private boolean isServerHealth = false;

  private final ThreadPoolTaskScheduler taskScheduler;

  public NamingServiceStandbyManager(NacosDiscoveryProperties properties) {
    this.properties = properties;
    this.taskScheduler = NamingServiceManagerUtils.buildTaskScheduler("Standby-Naming-Service-Check-Scheduler");
    if (properties.isMasterStandbyEnabled()) {
      startSchedulerTask();
    }
  }

  private void startSchedulerTask() {
    taskScheduler.scheduleWithFixedDelay(this::namingServiceHealthCheck,
        Duration.ofMillis(properties.getNamingServiceCheckTaskDelay()));
  }

  private void namingServiceHealthCheck() {
    if (namingService != null) {
      isServerHealth = NacosConst.STATUS_UP.equals(namingService.getServerStatus());
    }
  }

  @Override
  public NamingService getNamingService() {
    if (Objects.isNull(namingService)) {
      synchronized (NamingServiceStandbyManager.class) {
        if (Objects.isNull(namingService)) {
          try {
            namingService = new NacosNamingService(NamingServiceManagerUtils.buildStandbyServerProperties(properties));
            isServerHealth = NacosConst.STATUS_UP.equals(namingService.getServerStatus());
          } catch (Exception e) {
            LOGGER.error("build namingService failed.", e);
            throw new IllegalStateException("build namingService failed.", e);
          }
        }
      }
    }
    return namingService;
  }

  @Override
  public NamingMaintainService getNamingMaintainService() {
    if (Objects.isNull(namingMaintainService)) {
      synchronized (NamingServiceStandbyManager.class) {
        if (Objects.isNull(namingMaintainService)) {
          try {
            namingMaintainService
                = new NacosNamingMaintainService(NamingServiceManagerUtils.buildStandbyServerProperties(properties));
          } catch (Exception e) {
            LOGGER.error("build namingService failed.", e);
            throw new IllegalStateException("build namingService failed.", e);
          }
        }
      }
    }
    return namingMaintainService;
  }

  @Override
  public String getServerAddr() {
    return properties.getStandbyServerAddr();
  }

  @Override
  public int getOrder() {
    return properties.getOrder() + 10;
  }

  @Override
  public void shutDown() throws NacosException {
    if (Objects.nonNull(this.namingService)) {
      this.namingService.shutDown();
      this.namingService = null;
    }
    if (Objects.nonNull(this.namingMaintainService)) {
      this.namingMaintainService.shutDown();
      this.namingMaintainService = null;
    }
  }

  @Override
  public boolean checkNacosServerHealth() {
    return isServerHealth;
  }
}
