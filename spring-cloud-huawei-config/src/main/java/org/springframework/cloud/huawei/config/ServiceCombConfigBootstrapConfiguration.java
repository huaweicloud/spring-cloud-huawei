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

package org.springframework.cloud.huawei.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.common.transport.SSLConfig;
import org.springframework.cloud.common.transport.ServiceCombSSLProperties;
import org.springframework.cloud.huawei.config.client.ServiceCombConfigClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author wangqijun
 * @Date 11:00 2019-10-17
 **/
@Configuration
@ConditionalOnProperty(name = "spring.cloud.servicecomb.config.enabled", matchIfMissing = true)
public class ServiceCombConfigBootstrapConfiguration {


  @Bean
  @ConditionalOnMissingBean
  public ServiceCombConfigProperties serviceCombConfigProperties() {
    return new ServiceCombConfigProperties();
  }


  @Bean
  @ConditionalOnMissingBean
  public ServiceCombSSLProperties serviceCombSSLProperties() {
    return new ServiceCombSSLProperties();
  }

  @Bean
  public ServiceCombPropertySourceLocator serviceCombPropertySourceLocator(
      ServiceCombConfigProperties serviceCombConfigProperties,
      ServiceCombSSLProperties serviceCombSSLProperties) {
    ServiceCombConfigClientBuilder builder = new ServiceCombConfigClientBuilder();
    SSLConfig sslConfig = new SSLConfig();
    sslConfig.setEnable(serviceCombSSLProperties.isEnable());
    sslConfig.setAccessKey(serviceCombSSLProperties.getAccessKey());
    sslConfig.setSecretKey(serviceCombSSLProperties.getSecretKey());
    sslConfig.setAkskCustomCipher(serviceCombSSLProperties.getAkskCustomCipher());
    sslConfig.setProject(serviceCombSSLProperties.getProject());
    builder.setUrl(serviceCombConfigProperties.getAddress()).setSSLConfig(sslConfig);
    return new ServiceCombPropertySourceLocator(serviceCombConfigProperties, builder.createServiceCombConfigClient());
  }
}
