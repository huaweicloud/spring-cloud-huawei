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
package com.huaweicloud.service.engine.common.configration.bootstrap;

public class BootstrapProperties {
  private final MicroserviceProperties microserviceProperties;

  private final InstanceProperties instanceProperties;

  private final DiscoveryBootstrapProperties discoveryBootstrapProperties;

  private final ConfigBootstrapProperties configBootstrapProperties;

  private final ServiceCombSSLProperties serviceCombSSLProperties;

  private final ServiceCombAkSkProperties serviceCombAkSkProperties;

  private final ServiceCombRBACProperties serviceCombRBACProperties;

  public BootstrapProperties(
      MicroserviceProperties microserviceProperties,
      InstanceProperties instanceProperties,
      DiscoveryBootstrapProperties discoveryBootstrapProperties,
      ConfigBootstrapProperties configBootstrapProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties,
      ServiceCombRBACProperties serviceCombRBACProperties) {
    this.microserviceProperties = microserviceProperties;
    this.instanceProperties = instanceProperties;
    this.discoveryBootstrapProperties = discoveryBootstrapProperties;
    this.configBootstrapProperties = configBootstrapProperties;
    this.serviceCombSSLProperties = serviceCombSSLProperties;
    this.serviceCombAkSkProperties = serviceCombAkSkProperties;
    this.serviceCombRBACProperties = serviceCombRBACProperties;
  }

  public MicroserviceProperties getMicroserviceProperties() {
    return microserviceProperties;
  }

  public InstanceProperties getInstanceProperties() {
    return instanceProperties;
  }

  public DiscoveryBootstrapProperties getDiscoveryBootstrapProperties() {
    return discoveryBootstrapProperties;
  }

  public ConfigBootstrapProperties getConfigBootstrapProperties() {
    return configBootstrapProperties;
  }

  public ServiceCombSSLProperties getServiceCombSSLProperties() {
    return serviceCombSSLProperties;
  }

  public ServiceCombAkSkProperties getServiceCombAkSkProperties() {
    return serviceCombAkSkProperties;
  }

  public ServiceCombRBACProperties getServiceCombRBACProperties() {
    return serviceCombRBACProperties;
  }
}
