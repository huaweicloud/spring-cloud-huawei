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

package com.huaweicloud.config;

import java.util.List;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;

/**
 * bootstrap 配置信息。 由于使用 ContextRefresher 刷新配置， 会重新加载所有 bootstrap 的 bean， 因此配置中心的
 * 连接实例采用单例，保证只加载一次。
 */
@Configuration
@EnableConfigurationProperties(ServiceCombConfigProperties.class)
@ConditionalOnProperty(name = "spring.cloud.servicecomb.config.enabled", matchIfMissing = true)
public class ServiceCombConfigBootstrapConfiguration {
  @Bean
  public ServiceCombPropertySourceLocator serviceCombPropertySourceLocator(ServiceCombConfigProperties configProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties, ServiceCombSSLProperties serviceCombSSLProperties,
      List<AuthHeaderProvider> authHeaderProviders) {
    ConfigService.getInstance()
        .init(configProperties, serviceCombAkSkProperties, serviceCombSSLProperties, authHeaderProviders);
    return new ServiceCombPropertySourceLocator(ConfigService.getInstance().getConfigConverter());
  }
}
