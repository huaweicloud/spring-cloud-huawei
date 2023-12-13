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

package com.huaweicloud.governance.adapters.webclient;

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.handler.InstanceBulkheadHandler;
import org.apache.servicecomb.governance.handler.InstanceIsolationHandler;
import org.apache.servicecomb.governance.handler.RetryHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.governance.StatusCodeExtractor;

@Configuration
@ConditionalOnClass(name = {"org.springframework.web.reactive.function.client.WebClient"})
public class WebClientConfiguration {
  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBCLIENT_RETRY_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public ExchangeFilterFunction retryExchangeFilterFunction(RetryHandler retryHandler,
      GovernanceProperties governanceProperties) {
    return new RetryExchangeFilterFunction(retryHandler, governanceProperties);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBCLIENT_INSTANCE_BULKHEAD_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public ExchangeFilterFunction instanceBulkheadExchangeFilterFunction(InstanceBulkheadHandler bulkheadHandler,
      GovernanceProperties governanceProperties) {
    return new InstanceBulkheadExchangeFilterFunction(bulkheadHandler, governanceProperties);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBCLIENT_INSTANCE_ISOLATION_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public ExchangeFilterFunction instanceIsolationExchangeFilterFunction(InstanceIsolationHandler isolationHandler,
      GovernanceProperties governanceProperties) {
    return new InstanceIsolationExchangeFilterFunction(isolationHandler, governanceProperties);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBCLIENT_FAULT_INJECTION_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public ExchangeFilterFunction faultInjectionExchangeFilterFunction(FaultInjectionHandler faultInjectionHandler,
      GovernanceProperties governanceProperties) {
    return new FaultInjectionExchangeFilterFunction(governanceProperties, faultInjectionHandler);
  }

  @Bean
  public StatusCodeExtractor clientResponseStatusCodeExtractor(Environment environment) {
    return new ClientResponseStatusCodeExtractor(environment);
  }
}
