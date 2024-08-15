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

package com.huaweicloud.nacos.discovery.graceful;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.nacos.discovery.registry.NacosAutoServiceRegistration;
import com.huaweicloud.nacos.discovery.registry.NacosRegistration;
import com.huaweicloud.nacos.discovery.registry.NacosServiceRegistry;

@Endpoint(id = "nacos-service-registry")
public class NacosGracefulEndpoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(NacosGracefulEndpoint.class);

  private final NacosServiceRegistry nacosServiceRegistry;

  private final NacosRegistration nacosRegistration;

  private final NacosAutoServiceRegistration nacosAutoServiceRegistration;

  private boolean register_enabled = false;

  private boolean deregister_enabled = false;

  public NacosGracefulEndpoint(NacosServiceRegistry nacosServiceRegistry, NacosRegistration nacosRegistration,
      NacosAutoServiceRegistration nacosAutoServiceRegistration) {
    this.nacosServiceRegistry = nacosServiceRegistry;
    this.nacosRegistration = nacosRegistration;
    this.nacosAutoServiceRegistration = nacosAutoServiceRegistration;
    if (nacosRegistration.getNacosDiscoveryProperties().isRegisterEnabled()) {
      deregister_enabled = true;
    } else {
      register_enabled = true;
    }
  }

  @WriteOperation
  public void gracefulUpperAndDown(@Nullable String status) {
    if (GovernanceProperties.GRASEFUL_STATUS_UPPER.equalsIgnoreCase(status) && register_enabled) {
      nacosAutoServiceRegistration.setRegistryEnabled(true);
      nacosAutoServiceRegistration.registryExtend();
      register_enabled = false;
      deregister_enabled = true;
      return;
    }
    if (GovernanceProperties.GRASEFUL_STATUS_DOWN.equalsIgnoreCase(status) && deregister_enabled) {
      nacosServiceRegistry.deregister(nacosRegistration);
      register_enabled = true;
      deregister_enabled = false;
      return;
    }
    LOGGER.warn("operation is not allowed, status: " + status + ", register_enabled: " + register_enabled
        + ", deregister_enabled: " + deregister_enabled);
  }
}
