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

package com.huaweicloud.common.adapters.web;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class WebConfiguration {
  @Bean
  @ConditionalOnClass(name = {"org.springframework.http.client.ClientHttpRequestInterceptor",
      "org.springframework.web.client.RestTemplate"})
  public DecorateClientHttpRequestInterceptor decorateClientHttpRequestInterceptor(
      @Autowired(required = false) List<PreClientHttpRequestInterceptor> preClientHttpRequestInterceptors,
      @Autowired(required = false) List<PostClientHttpRequestInterceptor> postClientHttpRequestInterceptors,
      @Autowired(required = false) @LoadBalanced List<RestTemplate> restTemplates) {
    DecorateClientHttpRequestInterceptor interceptor = new DecorateClientHttpRequestInterceptor(
        preClientHttpRequestInterceptors,
        postClientHttpRequestInterceptors);

    if (restTemplates != null) {
      restTemplates.forEach(restTemplate -> restTemplate.getInterceptors().add(interceptor));
    }
    return interceptor;
  }

  @Bean
  @ConditionalOnBean(DecorateClientHttpRequestInterceptor.class)
  public PreClientHttpRequestInterceptor addContextPreClientHttpRequestInterceptor() {
    return new SerializeContextPreClientHttpRequestInterceptor();
  }
}
