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

package com.huaweicloud.common.transport;

import java.util.concurrent.TimeUnit;

import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.HttpClientProperties;

@Configuration
public class ClientTransportConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public HttpClient transportHttpClient(HttpClientProperties httpClientProperties) {
    PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager();
    pool.setDefaultMaxPerRoute(httpClientProperties.getPoolSizePerRoute());
    pool.setMaxTotal(httpClientProperties.getPoolSizeMax());
    pool.setDefaultConnectionConfig(ConnectionConfig.custom()
        .setConnectTimeout(httpClientProperties.getConnectTimeoutInMilliSeconds(), TimeUnit.MILLISECONDS)
        .setSocketTimeout(httpClientProperties.getReadTimeoutInMilliSeconds(), TimeUnit.MILLISECONDS)
        .build());
    return HttpClientBuilder.create()
        .setConnectionManager(pool)
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectionKeepAlive(
                TimeValue.of(httpClientProperties.getConnectionTimeToLiveInMilliSeconds(), TimeUnit.MILLISECONDS))
            .setConnectionRequestTimeout(
                Timeout.of(httpClientProperties.getConnectionRequestTimeoutInMilliSeconds(), TimeUnit.MILLISECONDS))
            .build())
        .evictExpiredConnections().evictIdleConnections(
            TimeValue.of(httpClientProperties.getConnectionIdleTimeoutInMilliSeconds(), TimeUnit.MILLISECONDS))
        .build();
  }
}
