/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.endpoint;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import com.huawei.cloud.nacos.config.manager.NacosConfigManager;

import org.springframework.boot.actuate.health.AbstractHealthIndicator;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

/**
 * Forked from com.alibaba.cloud.nacos.endpoint.NacosConfigHealthIndicator.java
 *
 * The {@link HealthIndicator} for Nacos Config.
 *
 * @author xiaojing
 * @author <a href="mailto:mercyblitz@gmail.com">Mercy</a>
 */
public class NacosConfigHealthIndicator extends AbstractHealthIndicator {

  private final List<NacosConfigManager> nacosConfigManagers;

  /**
   * status up .
   */
  private final static String STATUS_UP = "UP";

  /**
   * status down .
   */
  private final static String STATUS_DOWN = "DOWN";

  public NacosConfigHealthIndicator(List<NacosConfigManager> nacosConfigManagers) {
    this.nacosConfigManagers = nacosConfigManagers.stream()
        .sorted(Comparator.comparingInt(NacosConfigManager::getOrder)).collect(Collectors.toList());
  }

  @Override
  protected void doHealthCheck(Health.Builder builder) throws Exception {
    // Just return "UP" or "DOWN"
    String status = STATUS_DOWN;
    for (NacosConfigManager configManager: nacosConfigManagers) {
      if (configManager.checkServerConnect()) {
        status = configManager.getConfigService().getServerStatus();
      }
    }

    // Set the status to Builder
    builder.status(status);
    switch (status) {
      case STATUS_UP:
        builder.up();
        break;
      case STATUS_DOWN:
        builder.down();
        break;
      default:
        builder.unknown();
        break;
    }
  }
}
