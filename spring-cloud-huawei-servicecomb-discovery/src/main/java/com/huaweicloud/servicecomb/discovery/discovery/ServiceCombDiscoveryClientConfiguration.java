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

package com.huaweicloud.servicecomb.discovery.discovery;

import com.huaweicloud.common.exception.ServiceCombRuntimeException;
import com.huaweicloud.common.transport.ServiceCombSSLProperties;
import com.huaweicloud.servicecomb.discovery.registry.TagsProperties;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.CommonsClientAutoConfiguration;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.discovery.simple.SimpleDiscoveryClientAutoConfiguration;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import com.huaweicloud.servicecomb.discovery.ConditionalOnServiceCombDiscoveryEnabled;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClient;
import com.huaweicloud.servicecomb.discovery.client.ServiceCombClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

@Configuration
@ConditionalOnServiceCombDiscoveryEnabled
@AutoConfigureBefore({SimpleDiscoveryClientAutoConfiguration.class,
    CommonsClientAutoConfiguration.class})
public class ServiceCombDiscoveryClientConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ServiceCombDiscoveryProperties serviceCombProperties() {
    return new ServiceCombDiscoveryProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  public TagsProperties tagsProperties() {
    return new TagsProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceCombAkSkProperties serviceCombAkSkProperties() {
    return new ServiceCombAkSkProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  public ServiceCombSSLProperties serviceCombSSLProperties() {
    return new ServiceCombSSLProperties();
  }

  @Bean
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.discovery.enabled", matchIfMissing = true)
  public ServiceCombClient serviceCombClient(ServiceCombDiscoveryProperties serviceCombProperties,
      ServiceCombAkSkProperties serviceCombAkSkProperties, ServiceCombSSLProperties serviceCombSSLProperties) {
    ServiceCombClientBuilder builder = new ServiceCombClientBuilder();
    if (!StringUtils.isEmpty(serviceCombAkSkProperties.getEnable())) {
      throw new ServiceCombRuntimeException(
          "config credentials.enable has change to credentials.enabled ,old names are no longer supported, please change it.");
    }
    builder
        .setUrl(serviceCombProperties.getAddress())
        .setServiceCombAkSkProperties(serviceCombAkSkProperties)
        .setServiceCombSSLProperties(serviceCombSSLProperties);
    return builder.createServiceCombClient();
  }

  @Bean
  @ConditionalOnMissingBean
  public DiscoveryClient serviceCombDiscoveryClient(
      ServiceCombDiscoveryProperties discoveryProperties, ServiceCombClient serviceCombClient) {
    return new ServiceCombDiscoveryClient(discoveryProperties, serviceCombClient);
  }
}
