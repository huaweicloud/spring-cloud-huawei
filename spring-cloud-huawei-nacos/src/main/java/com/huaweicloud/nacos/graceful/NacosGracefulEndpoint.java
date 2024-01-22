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

package com.huaweicloud.nacos.graceful;

import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import com.alibaba.cloud.nacos.NacosDiscoveryProperties;
import com.alibaba.cloud.nacos.registry.NacosAutoServiceRegistration;
import com.alibaba.cloud.nacos.registry.NacosRegistration;
import com.alibaba.cloud.nacos.registry.NacosServiceRegistry;
import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

@Endpoint(id = "nacos-service-registry")
public class NacosGracefulEndpoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosGracefulEndpoint.class);

  private final NacosServiceRegistry nacosServiceRegistry;

  private final NacosRegistration nacosRegistration;

  private final NacosAutoServiceRegistration nacosAutoServiceRegistration;

  private final NacosDiscoveryProperties nacosDiscoveryProperties;

  private final AtomicBoolean isRegistry = new AtomicBoolean();

  public NacosGracefulEndpoint(NacosServiceRegistry nacosServiceRegistry, NacosRegistration nacosRegistration,
      NacosAutoServiceRegistration nacosAutoServiceRegistration, NacosDiscoveryProperties nacosDiscoveryProperties) {
    this.nacosServiceRegistry = nacosServiceRegistry;
    this.nacosRegistration = nacosRegistration;
    this.nacosAutoServiceRegistration = nacosAutoServiceRegistration;
    this.nacosDiscoveryProperties = nacosDiscoveryProperties;
  }

  @WriteOperation
  public void gracefulUpperAndDown(@Nullable String status) {
    if (StringUtils.isEmpty(status)) {
      return;
    }
    if (GovernanceProperties.GRASEFUL_STATUS_UPPER.equalsIgnoreCase(status) && !isRegistry.getAndSet(true)) {
      nacosDiscoveryProperties.setRegisterEnabled(true);
      nacosAutoServiceRegistration.start();
    } else if (GovernanceProperties.GRASEFUL_STATUS_DOWN.equalsIgnoreCase(status) && isRegistry.get()) {
      nacosServiceRegistry.deregister(nacosRegistration);
    } else {
      LOGGER.warn("operation is not allowed, status: " + status);
    }
  }
}
