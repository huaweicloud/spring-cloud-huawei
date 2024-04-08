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

package com.huaweicloud.nacos.discovery;

import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.client.naming.NacosNamingMaintainService;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.client.naming.NacosNamingService;

import java.util.Objects;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NamingServiceManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(NamingServiceManager.class);

  private final NacosDiscoveryProperties properties;

  private volatile NamingService namingService;

  private volatile NamingMaintainService namingMaintainService;

  public NamingServiceManager(NacosDiscoveryProperties properties) {
    this.properties = properties;
  }

  public NamingService buildNamingService() {
    if (Objects.isNull(namingService)) {
      synchronized (NamingServiceManager.class) {
        if (Objects.isNull(namingService)) {
          try {
            namingService = new NacosNamingService(getProperties(properties));
          } catch (Exception e) {
            LOGGER.error("build namingService failed.", e);
            throw new IllegalStateException("build namingService failed.", e);
          }
        }
      }
    }
    return namingService;
  }

  public NamingMaintainService buildNamingMaintainService() {
    if (Objects.isNull(namingMaintainService)) {
      synchronized (NamingServiceManager.class) {
        if (Objects.isNull(namingMaintainService)) {
          try {
            namingMaintainService = new NacosNamingMaintainService(getProperties(properties));
          } catch (Exception e) {
            LOGGER.error("build namingMaintainService failed.", e);
            throw new IllegalStateException("build namingMaintainService failed.", e);
          }
        }
      }
    }
    return namingMaintainService;
  }

  private static Properties getProperties(NacosDiscoveryProperties nacosDiscoveryProperties) {
    Properties properties = new Properties();
    properties.put(NacosConst.NAMESPACE, nacosDiscoveryProperties.getNamespace());
    properties.put(NacosConst.SERVER_ADDR, nacosDiscoveryProperties.getServerAddr());
    if (nacosDiscoveryProperties.getUsername() != null) {
      properties.put(NacosConst.USERNAME, nacosDiscoveryProperties.getUsername());
    }
    if (nacosDiscoveryProperties.getPassword() != null) {
      properties.put(NacosConst.PASSWORD, nacosDiscoveryProperties.getPassword());
    }
    if (nacosDiscoveryProperties.getAccessKey() != null) {
      properties.put(NacosConst.ACCESS_KEY, nacosDiscoveryProperties.getAccessKey());
    }
    if (nacosDiscoveryProperties.getSecretKey() != null) {
      properties.put(NacosConst.SECRET_KEY, nacosDiscoveryProperties.getSecretKey());
    }
    if (nacosDiscoveryProperties.getLogName() != null) {
      properties.put(NacosConst.NACOS_NAMING_LOG_NAME, nacosDiscoveryProperties.getLogName());
    }

    properties.put(NacosConst.CLUSTER_NAME, nacosDiscoveryProperties.getClusterName());
    properties.put(NacosConst.NAMING_LOAD_CACHE_AT_START, nacosDiscoveryProperties.getNamingLoadCacheAtStart());
    return properties;
  }

  public void nacosServiceShutDown() throws NacosException {
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
