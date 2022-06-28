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

package com.huaweicloud.governance.adapters.feign;

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.apache.servicecomb.governance.handler.ext.ClientRecoverPolicy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Client;
import feign.Response;

@Configuration
@ConditionalOnClass(name = {"org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient"})
public class FeignConfiguration {
  @Bean
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.feign.governance.enabled",
      havingValue = "true", matchIfMissing = true)
  public Client feignClient(RetryHandler retryHandler,
      InstanceIsolationHandler instanceIsolationHandler,
      @Autowired(required = false) FaultInjectionHandler faultInjectionHandler,
      @Autowired(required = false) ClientRecoverPolicy<Response> clientRecoverPolicy,
      LoadBalancerClient loadBalancerClient,
      LoadBalancerClientFactory loadBalancerClientFactory) {
    return new RetryableFeignBlockingLoadBalancerClient(
        retryHandler, instanceIsolationHandler, faultInjectionHandler, clientRecoverPolicy,
        new Client.Default(null, null), loadBalancerClient,
        loadBalancerClientFactory);
  }

  @Bean
  public ResponseStatusCodeExtractor responseStatusCodeExtractor() {
    return new ResponseStatusCodeExtractor();
  }
}
