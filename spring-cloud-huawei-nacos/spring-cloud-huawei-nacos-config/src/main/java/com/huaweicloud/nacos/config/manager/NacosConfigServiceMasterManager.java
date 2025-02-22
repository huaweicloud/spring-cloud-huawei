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

package com.huaweicloud.nacos.config.manager;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.cloud.nacos.NacosConfigProperties;
import com.alibaba.cloud.nacos.diagnostics.analyzer.NacosConnectionFailureException;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.config.ConfigService;
import com.huaweicloud.nacos.config.NacosConfigConst;

public class NacosConfigServiceMasterManager implements NacosConfigManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosConfigServiceMasterManager.class);

  private final NacosConfigProperties properties;

  private volatile ConfigService configService;

  private final ScheduledExecutorService taskScheduler;

  private volatile boolean isServerHealth = false;

  public NacosConfigServiceMasterManager(NacosConfigProperties properties) {
    this.properties = properties;
    checkConfigServiceHealth();
    this.taskScheduler = Executors.newScheduledThreadPool(1, (t) -> new Thread(t, "Master-Config-check"));
    startSchedulerTask();
  }

  @Override
  public ConfigService getConfigService() {
    if (Objects.isNull(configService)) {
      synchronized (NacosConfigServiceMasterManager.class) {
        if (Objects.isNull(configService)) {
          try {
            configService = NacosFactory.createConfigService(properties.assembleMasterNacosServerProperties());
          } catch (Exception e) {
            LOGGER.error("build nacosConfigServiceMaster failed.", e);
            throw new NacosConnectionFailureException(properties.getServerAddr(), e.getMessage(), e);
          }
        }
      }
    }
    return configService;
  }

  private void startSchedulerTask() {
    long delay = properties.getMasterStandbyServerTaskDelay();
    taskScheduler.scheduleWithFixedDelay(this::checkConfigServiceHealth, delay, delay, TimeUnit.MILLISECONDS);
  }

  private void checkConfigServiceHealth() {
    isServerHealth = ConfigServiceManagerUtils.checkConfigServerHealth(properties.getServerAddr(),
        properties.assembleMasterNacosServerProperties());
  }

  @Override
  public String getServerAddr() {
    return properties.getServerAddr();
  }

  @Override
  public int getOrder() {
    return properties.getOrder();
  }

  @Override
  public boolean isNacosServerHealth() {
    return isServerHealth;
  }

  @Override
  public void resetConfigService() {
    this.configService = null;
    this.isServerHealth = false;
  }

  @Override
  public boolean isMasterConfigService() {
    return true;
  }

  @Override
  public boolean isRpcConnectHealth() {
    if (configService == null) {
      return false;
    }
    return NacosConfigConst.STATUS_UP.equals(configService.getServerStatus());
  }
}
