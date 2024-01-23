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

import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.HttpClientProperties;

@Configuration
public class ClientTransportConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public HttpClient transportHttpClient(HttpClientProperties httpClientProperties) {
    PoolingHttpClientConnectionManager pool = new PoolingHttpClientConnectionManager(
        httpClientProperties.getConnectionTimeToLiveInMilliSeconds(), TimeUnit.MILLISECONDS);
    pool.setDefaultMaxPerRoute(httpClientProperties.getPoolSizePerRoute());
    pool.setMaxTotal(httpClientProperties.getPoolSizeMax());
    return HttpClientBuilder.create()
        .setConnectionManager(pool)
        .setDefaultRequestConfig(RequestConfig.custom()
            .setConnectTimeout(httpClientProperties.getConnectTimeoutInMilliSeconds())
            .setConnectionRequestTimeout(httpClientProperties.getConnectionRequestTimeoutInMilliSeconds())
            .setSocketTimeout(httpClientProperties.getReadTimeoutInMilliSeconds())
            .build())
        .evictExpiredConnections().evictIdleConnections(
            httpClientProperties.getConnectionIdleTimeoutInMilliSeconds(), TimeUnit.MILLISECONDS)
        .build();
  }
}
