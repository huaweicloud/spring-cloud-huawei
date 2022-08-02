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

package com.huaweicloud.governance.adapters.web;

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.huaweicloud.common.configration.dynamic.HttpClientProperties;

@Configuration
@ConditionalOnClass(name = {"org.springframework.http.client.ClientHttpRequestInterceptor",
    "org.springframework.web.client.RestTemplate"})
public class WebConfiguration {
  @Bean
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.restTemplate.retryable.enabled",
      havingValue = "true", matchIfMissing = true)
  @LoadBalanced
  @Primary
  public RestTemplate retryableRestTemplate(RetryHandler retryHandler,
      FaultInjectionHandler faultInjectionHandler,
      @Autowired(required = false) ClientRecoverPolicy<Object> recoverPolicy,
      HttpClientProperties httpClientProperties) {
    GovernanceRestTemplate restTemplate = new GovernanceRestTemplate(retryHandler, faultInjectionHandler, recoverPolicy);
    restTemplate.setRequestFactory(getClientHttpRequestFactory(httpClientProperties));
    return restTemplate;
  }

  private ClientHttpRequestFactory getClientHttpRequestFactory(HttpClientProperties httpClientProperties) {
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
        new HttpComponentsClientHttpRequestFactory();
    clientHttpRequestFactory.setConnectTimeout(httpClientProperties.getConnectTimeoutInMilliSeconds());
    clientHttpRequestFactory.setReadTimeout(httpClientProperties.getReadTimeoutInMilliSeconds());
    return clientHttpRequestFactory;
  }

  @Bean
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.restTemplate.isolation.enabled",
      havingValue = "true", matchIfMissing = true)
  public ClientHttpRequestInterceptor isolationClientHttpRequestInterceptor(InstanceIsolationHandler isolationHandler,
      @Autowired(required = false) ClientRecoverPolicy<ClientHttpResponse> recoverPolicy) {
    return new IsolationClientHttpRequestInterceptor(isolationHandler, recoverPolicy);
  }

  @Bean
  public ClientHttpResponseStatusCodeExtractor clientHttpResponseStatusCodeExtractor() {
    return new ClientHttpResponseStatusCodeExtractor();
  }
}
