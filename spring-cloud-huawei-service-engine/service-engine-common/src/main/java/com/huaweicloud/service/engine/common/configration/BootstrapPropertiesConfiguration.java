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

package com.huaweicloud.service.engine.common.configration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.service.engine.common.configration.bootstrap.BootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ConfigBootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.DiscoveryBootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.InstanceProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.MicroserviceProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombAkSkProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombRBACProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombSSLProperties;

@Configuration
public class BootstrapPropertiesConfiguration {
  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.service")
  public MicroserviceProperties microserviceProperties() {
    return new MicroserviceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.instance")
  public InstanceProperties instanceProperties() {
    return new InstanceProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.credentials")
  public ServiceCombAkSkProperties serviceCombAkSkProperties() {
    return new ServiceCombAkSkProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.credentials.account")
  public ServiceCombRBACProperties serviceCombRBACProperties() {
    return new ServiceCombRBACProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.ssl")
  public ServiceCombSSLProperties serviceCombSSLProperties() {
    return new ServiceCombSSLProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.discovery")
  public DiscoveryBootstrapProperties discoveryBootstrapProperties() {
    return new DiscoveryBootstrapProperties();
  }

  @Bean
  @ConfigurationProperties("spring.cloud.servicecomb.config")
  public ConfigBootstrapProperties configBootstrapProperties() {
    return new ConfigBootstrapProperties();
  }

  @Bean
  public BootstrapProperties bootstrapProperties(MicroserviceProperties microserviceProperties,
      InstanceProperties instanceProperties,
      DiscoveryBootstrapProperties discoveryBootstrapProperties,
      ConfigBootstrapProperties configBootstrapProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties,
      ServiceCombRBACProperties serviceCombRBACProperties) {
    return new BootstrapProperties(microserviceProperties, instanceProperties, discoveryBootstrapProperties,
        configBootstrapProperties, serviceCombSSLProperties, serviceCombAkSkProperties, serviceCombRBACProperties);
  }
}
