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

package com.huaweicloud.governance.adapters.loadbalancer.weightedResponseTime;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class ServiceInstanceMetrics {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInstanceMetrics.class);

  public static final Cache<String, ServerMetrics> INSTANCE_SERVER_METRICS_MAP = CacheBuilder.newBuilder()
      .expireAfterAccess(12 * 60 * 60 * 1000, TimeUnit.MILLISECONDS)
      .build();

  public static ServerMetrics getMetrics(ServiceInstance instance) {
    try {
      return INSTANCE_SERVER_METRICS_MAP.get(buildKey(instance), ServerMetrics::new);
    } catch (Exception e) {
      LOGGER.error("serverMetrics load failed.");
      return new ServerMetrics();
    }
  }

  private static String buildKey(ServiceInstance instance) {
    if (StringUtils.isEmpty(instance.getInstanceId())) {
      String result = instance.getHost() + ":" + instance.getPort();
      return result.replaceAll("[^0-9a-zA-Z]", "-");
    }
    return instance.getInstanceId();
  }
}
