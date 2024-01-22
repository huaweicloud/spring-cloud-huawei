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

package com.huaweicloud.common.adapters.web;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.client.ClientHttpRequestInterceptor;

import com.huaweicloud.common.configration.dynamic.ContextProperties;

@Configuration
@ConditionalOnClass(name = {"org.springframework.http.client.ClientHttpRequestInterceptor",
    "org.springframework.web.client.RestTemplate"})
@ConditionalOnProperty(value = ContextProperties.REST_TEMPLATE_CONTEXT_ENABLED,
    havingValue = "true", matchIfMissing = true)
public class WebConfiguration {
  @Bean
  public ClientHttpRequestInterceptor decorateClientHttpRequestInterceptor(
      @Autowired(required = false) List<PreClientHttpRequestInterceptor> preClientHttpRequestInterceptors,
      @Autowired(required = false) List<PostClientHttpRequestInterceptor> postClientHttpRequestInterceptors) {
    return new DecorateClientHttpRequestInterceptor(
        preClientHttpRequestInterceptors,
        postClientHttpRequestInterceptors);
  }

  @Bean
  public ClientHttpRequestInterceptor invocationContextClientHttpRequestInterceptor(ContextProperties contextProperties) {
    return new InvocationContextClientHttpRequestInterceptor(contextProperties);
  }

  @Bean
  public ClientHttpRequestInterceptor metricsClientHttpRequestInterceptor() {
    return new MetricsClientHttpRequestInterceptor();
  }

  @Bean
  public ClientHttpRequestInterceptor serializeContextClientHttpRequestInterceptor() {
    return new SerializeContextClientHttpRequestInterceptor();
  }

  @Bean
  public ClientHttpRequestInterceptor restTemplateAddServiceNameContext(
      @Autowired(required = false) Registration registration) {
    return new RestTemplateAddServiceNameContext(registration);
  }

  @Bean
  // sort ClientHttpRequestInterceptors.
  // If ClientHttpRequestInterceptor does not implement Ordered, executed first, and then ordered .
  // And make LoadBalancerInterceptor the first ordered ClientHttpRequestInterceptor.
  public RestTemplateCustomizer restTemplateCustomizer(List<ClientHttpRequestInterceptor> interceptors) {
    return restTemplate -> {
      List<ClientHttpRequestInterceptor> nonOrderedList = new ArrayList<>();
      List<ClientHttpRequestInterceptor> orderedList = new ArrayList<>();
      LoadBalancerInterceptor loadBalancerInterceptor = null;

      for (ClientHttpRequestInterceptor interceptor : interceptors) {
        if (interceptor instanceof LoadBalancerInterceptor) {
          loadBalancerInterceptor = (LoadBalancerInterceptor) interceptor;
          continue;
        }
        if (interceptor instanceof Ordered) {
          orderedList.add(interceptor);
        } else {
          nonOrderedList.add(interceptor);
        }
      }
      orderedList.sort(Comparator.comparingInt(a -> ((Ordered) a).getOrder()));
      if (loadBalancerInterceptor != null) {
        nonOrderedList.add(loadBalancerInterceptor);
      }
      nonOrderedList.addAll(orderedList);

      // avoid restTemplate sort himself
      restTemplate.setInterceptors(new ArrayList<>());
      restTemplate.getInterceptors().addAll(nonOrderedList);
    };
  }
}
