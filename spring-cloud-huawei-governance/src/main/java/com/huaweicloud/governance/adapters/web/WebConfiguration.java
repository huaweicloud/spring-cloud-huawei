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

import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
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
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.common.configration.dynamic.HttpClientProperties;
import com.huaweicloud.governance.authentication.consumer.RSAConsumerTokenManager;

@Configuration
@ConditionalOnClass(name = {"org.springframework.http.client.ClientHttpRequestInterceptor",
    "org.springframework.web.client.RestTemplate"})
public class WebConfiguration {
  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.REST_TEMPLATE_RETRY_ENABLED,
      havingValue = "true", matchIfMissing = true)
  @LoadBalanced
  @Primary
  public RestTemplate governanceRestTemplate(RetryHandler retryHandler,
      FaultInjectionHandler faultInjectionHandler,
      @Autowired(required = false) ClientRecoverPolicy<Object> recoverPolicy,
      HttpClientProperties httpClientProperties) {
    GovernanceRestTemplate restTemplate = new GovernanceRestTemplate(retryHandler, faultInjectionHandler,
        recoverPolicy);
    restTemplate.setRequestFactory(getClientHttpRequestFactory(httpClientProperties));
    return restTemplate;
  }

  private ClientHttpRequestFactory getClientHttpRequestFactory(HttpClientProperties httpClientProperties) {
    PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
    pool.setDefaultMaxPerRoute(httpClientProperties.getPoolSizePerRoute());
    pool.setMaxTotal(httpClientProperties.getPoolSizeMax());
    HttpComponentsClientHttpRequestFactory clientHttpRequestFactory =
        new HttpComponentsClientHttpRequestFactory(HttpClientBuilder.create().setConnectionManager(pool).build());
    clientHttpRequestFactory.setConnectionRequestTimeout(
        httpClientProperties.getConnectionRequestTimeoutInMilliSeconds());
    clientHttpRequestFactory.setConnectTimeout(httpClientProperties.getConnectTimeoutInMilliSeconds());
    clientHttpRequestFactory.setReadTimeout(httpClientProperties.getReadTimeoutInMilliSeconds());
    return clientHttpRequestFactory;
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.REST_TEMPLATE_INSTANCE_ISOLATION_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public ClientHttpRequestInterceptor isolationClientHttpRequestInterceptor(InstanceIsolationHandler isolationHandler) {
    return new IsolationClientHttpRequestInterceptor(isolationHandler);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.REST_TEMPLATE_INSTANCE_BULKHEAD_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public ClientHttpRequestInterceptor bulkheadClientHttpRequestInterceptor(InstanceBulkheadHandler bulkheadHandler) {
    return new BulkheadClientHttpRequestInterceptor(bulkheadHandler);
  }

  @Bean
  public ClientHttpResponseStatusCodeExtractor clientHttpResponseStatusCodeExtractor() {
    return new ClientHttpResponseStatusCodeExtractor();
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED,
      havingValue = "true")
  public RestTemplateAddTokenContext restTemplateAddTokenContext(RSAConsumerTokenManager authenticationTokenManager) {
    return new RestTemplateAddTokenContext(authenticationTokenManager);
  }
}
