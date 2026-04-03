/*

 * Copyright (C) 2020-2026 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.common.adapters.feign;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.hc.core5.util.TimeValue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.openfeign.clientconfig.HttpClient5FeignConfiguration.HttpClientBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.configration.dynamic.HttpClientProperties;

import feign.RequestInterceptor;

@Configuration
@ConditionalOnClass(name = {"feign.RequestInterceptor"})
@ConditionalOnProperty(value = ContextProperties.FEIGN_CONTEXT_ENABLED,
    havingValue = "true", matchIfMissing = true)
public class FeignConfiguration {
  @Bean
  public RequestInterceptor decorateRequestInterceptor(
      List<OrderedRequestInterceptor> orderedRequestInterceptors) {
    return new DecorateRequestInterceptor(orderedRequestInterceptors);
  }

  @Bean
  public RequestInterceptor invocationContextRequestInterceptor(ContextProperties contextProperties) {
    return new InvocationContextRequestInterceptor(contextProperties);
  }

  @Bean
  public RequestInterceptor serializeContextOrderedRequestInterceptor() {
    return new SerializeContextOrderedRequestInterceptor();
  }

  @Bean
  public FeignAddServiceNameContext feignAddServiceNameContext(@Autowired(required = false) Registration registration) {
    return new FeignAddServiceNameContext(registration);
  }

  /**
   * httpclient5 5.6 dependency, HttpClient5FeignConfiguration build CloseableHttpClient parameter have no maxIdleTime.
   * As a result, a null pointer exception occurs during startup.
   * If HttpClientBuilder in the later version is compatible with the null value of maxIdleTime
   * or the maxIdleTime value is set when CloseableHttpClient is constructed in HttpClient5FeignConfiguration,
   * this method can be deleted.
   *
   * @param httpClientProperties httpClientProperties
   * @return HttpClientBuilderCustomizer
   */
  @Bean
  public HttpClientBuilderCustomizer feignExtendHttpClientBuilderCustomizer(HttpClientProperties httpClientProperties) {
    return builder -> builder.evictIdleConnections(
        TimeValue.of(httpClientProperties.getConnectionIdleTimeoutInMilliSeconds(), TimeUnit.MILLISECONDS));
  }
}
