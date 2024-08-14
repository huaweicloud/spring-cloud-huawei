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

package com.huaweicloud.nacos.config.client;

import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.util.CollectionUtils;

import com.alibaba.cloud.nacos.NacosPropertySourceRepository;
import com.alibaba.cloud.nacos.refresh.NacosContextRefresher;

public class LabelRouterConfigListener {
  private final ThreadPoolTaskScheduler taskScheduler;

  private final Set<String> listenersKey;

  private final NacosContextRefresher contextRefresher;

  public LabelRouterConfigListener(NacosContextRefresher contextRefresher, Set<String> listenersKey) {
    this.listenersKey = new HashSet<>(listenersKey);
    this.contextRefresher = contextRefresher;
    this.taskScheduler = buildTaskScheduler();
  }

  public void schedulerCheckLabelRouterConfig() {
    taskScheduler.scheduleWithFixedDelay(this::checkLabelRouterConfig, Duration.ofMillis(15000));
  }

  private void checkLabelRouterConfig() {
    NacosPropertiesFuzzyQueryService blurQueryService = NacosPropertiesFuzzyQueryService.getInstance();
    List<PropertyConfigItem> routerProperties = blurQueryService.loadRouterProperties();
    if (CollectionUtils.isEmpty(routerProperties)) {
      return;
    }
    for (PropertyConfigItem configItem : routerProperties) {
      String key = NacosPropertySourceRepository.getMapKey(configItem.getDataId(), configItem.getGroup());
      if (!listenersKey.contains(key)) {
        contextRefresher.registerAddRouterConfigListener(configItem.getDataId(), configItem.getGroup());
        listenersKey.add(key);
      }
    }
  }

  private ThreadPoolTaskScheduler buildTaskScheduler() {
    ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
    taskScheduler.setBeanName("Nacos-Router-Config-Listener-Scheduler");
    taskScheduler.initialize();
    return taskScheduler;
  }
}
