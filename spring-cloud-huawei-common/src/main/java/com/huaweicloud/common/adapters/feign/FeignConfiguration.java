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

package com.huaweicloud.common.adapters.feign;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.access.AccessLogLogger;
import com.huaweicloud.common.configration.dynamic.ContextProperties;

import feign.RequestInterceptor;
import feign.ResponseInterceptor;

@Configuration
@ConditionalOnClass(name = {"feign.RequestInterceptor"})
public class FeignConfiguration {
  @Bean
  public RequestInterceptor decorateRequestInterceptor(
      List<OrderedRequestInterceptor> orderedRequestInterceptors) {
    return new DecorateRequestInterceptor(orderedRequestInterceptors);
  }

  @Bean
  public RequestInterceptor invocationContextRequestInterceptor() {
    return new InvocationContextRequestInterceptor();
  }

  @Bean
  public RequestInterceptor serializeContextOrderedRequestInterceptor() {
    return new SerializeContextOrderedRequestInterceptor();
  }

  @Bean
  public RequestInterceptor accessLogRequestInterceptor(ContextProperties contextProperties,
      AccessLogLogger accessLogLogger) {
    return new AccessLogRequestInterceptor(contextProperties, accessLogLogger);
  }

  @Bean
  public ResponseInterceptor responseInterceptor(ContextProperties contextProperties,
      AccessLogLogger accessLogLogger) {
    // require feign 11.9.1+
    return new AccessLogResponseInterceptor(contextProperties, accessLogLogger);
  }
}
