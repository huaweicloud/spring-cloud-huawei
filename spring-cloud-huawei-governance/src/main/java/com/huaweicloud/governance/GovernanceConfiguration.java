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
package com.huaweicloud.governance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.apache.servicecomb.governance.InvocationContext;
import org.apache.servicecomb.governance.MicroserviceMeta;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.RetryExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingClass;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.client.loadbalancer.LoadBalancerInterceptor;
import org.springframework.cloud.client.loadbalancer.RestTemplateCustomizer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import com.huaweicloud.common.event.ConfigRefreshEvent;

@Configuration
@ComponentScan(basePackages = {"org.apache.servicecomb.governance"})
public class GovernanceConfiguration {

  @Bean
  @ConditionalOnClass(name = "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter")
  public GovernanceRequestMappingHandlerAdapter governanceRequestMappingHandlerAdapter(RateLimitingHandler rateLimitingHandler, CircuitBreakerHandler circuitBreakerHandler, BulkheadHandler bulkheadHandler
) {
    return new GovernanceRequestMappingHandlerAdapter(rateLimitingHandler,circuitBreakerHandler,bulkheadHandler);
}

  @Bean
  public ApplicationListener<ConfigRefreshEvent> governanceApplicationListener() {
    return configRefreshEvent -> GovernanceEventManager
        .post(new GovernanceConfigurationChangedEvent(new HashSet<>(configRefreshEvent.getChange())));
  }

  @Bean
  @ConditionalOnClass(value = RestTemplate.class)
  public GovernanceClientHttpRequestInterceptor governanceClientHttpRequestInterceptor(
          @Autowired(required = false) @LoadBalanced List<RestTemplate> restTemplates, RetryHandler retryHandler) {
    return new GovernanceClientHttpRequestInterceptor(retryHandler);
  }

  // 使用 RestTemplateCustomizer 保证在 restTemplateCustomizer 后面执行。 如果项目出现冲突，
  // 用户需要自己定义 RestTemplateCustomizer，参考 LoadBalancerAutoConfiguration
  // 这里也指定为 ConditionalOnMissingBean。
  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnMissingClass("org.springframework.retry.support.RetryTemplate")
  public RestTemplateCustomizer governanceRestTemplateCustomizer(
      final GovernanceClientHttpRequestInterceptor governanceClientHttpRequestInterceptor,
      final LoadBalancerInterceptor loadBalancerInterceptor) {
    return restTemplate -> {
      List<ClientHttpRequestInterceptor> list = new ArrayList<>(
          restTemplate.getInterceptors());
      list.add(loadBalancerInterceptor);
      list.add(governanceClientHttpRequestInterceptor);
      restTemplate.setInterceptors(list);
    };
  }

  @Bean
  @ConditionalOnClass(name = "org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient")
  public GovernanceFeignClient governanceFeignClient(RetryHandler retryHandler) {
    return new GovernanceFeignClient(retryHandler);
  }

  @Bean
  public MicroserviceMeta governanceMicroserviceMeta() {
    return new SpringCloudMicroserviceMeta();
  }

  @Bean
  public InvocationContext governanceInvocationContext() {
    return new SpringCloudInvocationContext();
  }

  @Bean
  public RetryExtension governanceRetryExtension() {
    return new SpringCloudRetryExtension();
  }
}
