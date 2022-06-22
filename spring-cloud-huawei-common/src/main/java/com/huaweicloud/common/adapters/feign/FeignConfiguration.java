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

import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.ContextProperties;

@Configuration
@ConditionalOnClass(name = {"feign.RequestInterceptor"})
public class FeignConfiguration {
  @Bean
  public DecorateRequestInterceptor decorateRequestInterceptor(
      List<OrderedRequestInterceptor> orderedRequestInterceptors) {
    return new DecorateRequestInterceptor(orderedRequestInterceptors);
  }

  @Bean
  @ConditionalOnBean(DecorateRequestInterceptor.class)
  public OrderedRequestInterceptor serializeContextOrderedRequestInterceptor() {
    return new SerializeContextOrderedRequestInterceptor();
  }

  @Bean
  @ConditionalOnBean(DecorateRequestInterceptor.class)
  public TraceIdOrderedRequestInterceptor traceIdOrderedRequestInterceptor(ContextProperties contextProperties) {
    return new TraceIdOrderedRequestInterceptor(contextProperties);
  }
}