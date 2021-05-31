/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.common;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.transport.AkSkRequestAuthHeaderProvider;
import com.huaweicloud.common.transport.RBACRequestAuthHeaderProvider;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import com.huaweicloud.common.transport.DiscoveryBootstrapProperties;
import com.huaweicloud.common.transport.ServiceCombRBACProperties;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import com.huaweicloud.common.util.Cipher;
import com.huaweicloud.common.util.ShaAKSKCipher;

@Configuration
@EnableConfigurationProperties({ServiceCombAkSkProperties.class, ServiceCombRBACProperties.class,
    ServiceCombSSLProperties.class, DiscoveryBootstrapProperties.class})
public class CommonConfiguration {
  @Bean
  public Cipher shaAKSKCipher() {
    return new ShaAKSKCipher();
  }

  @Bean
  public AuthHeaderProvider akSkRequestAuthHeaderProvider(ServiceCombAkSkProperties serviceCombAkSkProperties) {
    return new AkSkRequestAuthHeaderProvider(serviceCombAkSkProperties);
  }

  @Bean
  public AuthHeaderProvider rbacRequestAuthHeaderProvider(DiscoveryBootstrapProperties discoveryProperties,
      ServiceCombSSLProperties serviceCombSSLProperties,
      ServiceCombRBACProperties serviceCombRBACProperties) {
    return new RBACRequestAuthHeaderProvider(discoveryProperties, serviceCombSSLProperties, serviceCombRBACProperties);
  }
}
