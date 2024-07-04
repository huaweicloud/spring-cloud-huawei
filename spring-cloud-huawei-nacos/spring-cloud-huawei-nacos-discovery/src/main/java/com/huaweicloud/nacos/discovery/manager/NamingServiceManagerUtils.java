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

import java.util.Properties;

import com.huaweicloud.nacos.discovery.NacosConst;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;

public class NamingServiceManagerUtils {
  public static Properties buildMasterServerProperties(NacosDiscoveryProperties properties) {
    return buildProperties(properties, properties.getServerAddr());
  }

  public static Properties buildStandbyServerProperties(NacosDiscoveryProperties properties) {
    return buildProperties(properties, properties.getStandbyServerAddr());
  }

  private static Properties buildProperties(NacosDiscoveryProperties nacosDiscoveryProperties, String address) {
    Properties properties = new Properties();
    properties.put(NacosConst.NAMESPACE, nacosDiscoveryProperties.getNamespace());
    properties.put(NacosConst.SERVER_ADDR, address);
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
}
