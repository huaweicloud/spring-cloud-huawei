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

import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingService;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NamingServiceMasterManager implements NamingServiceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(NamingServiceMasterManager.class);

  private final NacosDiscoveryProperties properties;

  private volatile NamingService namingService;

  private volatile NamingMaintainService namingMaintainService;

  public NamingServiceMasterManager(NacosDiscoveryProperties properties) {
    this.properties = properties;
  }

  @Override
  public NamingService getNamingService() {
    if (Objects.isNull(namingService)) {
      synchronized (NamingServiceMasterManager.class) {
        if (Objects.isNull(namingService)) {
          try {
            namingService = new NacosNamingService(NamingServiceManagerUtils.buildMasterServerProperties(properties));
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
      synchronized (NamingServiceMasterManager.class) {
        if (Objects.isNull(namingMaintainService)) {
          try {
            namingMaintainService
                = new NacosNamingMaintainService(NamingServiceManagerUtils.buildMasterServerProperties(properties));
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
    return properties.getServerAddr();
  }

  @Override
  public int getOrder() {
    return properties.getOrder();
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
}
