/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.servicecomb.discovery.adapters.web;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.service.engine.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.servicecomb.discovery.ConditionalOnServiceCombDiscoveryEnabled;
import com.huaweicloud.servicecomb.discovery.authentication.consumer.RSAConsumerTokenManager;
import com.huaweicloud.servicecomb.discovery.authentication.consumer.RestTemplateAddTokenContext;

@Configuration
@ConditionalOnClass(name = {"org.springframework.http.client.ClientHttpRequestInterceptor",
    "org.springframework.web.client.RestTemplate"})
public class WebConfiguration {
  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED,
      havingValue = "true")
  @ConditionalOnServiceCombDiscoveryEnabled
  public RestTemplateAddTokenContext restTemplateAddTokenContext(RSAConsumerTokenManager authenticationTokenManager) {
    return new RestTemplateAddTokenContext(authenticationTokenManager);
  }
}
