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

import com.huaweicloud.common.util.SecretUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.huaweicloud.common.transport.AkSkConfig;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import com.huaweicloud.config.client.ServiceCombConfigClient;
import com.huaweicloud.config.client.ServiceCombConfigClientBuilder;
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
  public ServiceCombAkSkProperties serviceCombSSLProperties() {
    return new ServiceCombAkSkProperties();
  }

  @Bean
  public ServiceCombConfigClient serviceCombConfigClient(
      ServiceCombConfigProperties serviceCombConfigProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties) {
    ServiceCombConfigClientBuilder builder = new ServiceCombConfigClientBuilder();
    AkSkConfig akSkConfig = SecretUtil.generateSSLConfig(serviceCombAkSkProperties);
    builder.setUrl(serviceCombConfigProperties.getServerAddr()).setSSLConfig(akSkConfig);
    return builder.createServiceCombConfigClient();
  }
  @Bean
  public ServiceCombPropertySourceLocator serviceCombPropertySourceLocator(
      ServiceCombConfigProperties serviceCombConfigProperties,
      ServiceCombConfigClient serviceCombConfigClient,
      ServiceCombAkSkProperties serviceCombAkSkProperties) {
    return new ServiceCombPropertySourceLocator(serviceCombConfigProperties,
        serviceCombConfigClient,
        serviceCombAkSkProperties.getProject());
  }
}
