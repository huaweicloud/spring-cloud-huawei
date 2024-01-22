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

package com.huaweicloud.common.adapters.webclient;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cloud.client.loadbalancer.reactive.DeferringLoadBalancerExchangeFilterFunction;
import org.springframework.cloud.client.loadbalancer.reactive.ReactorLoadBalancerExchangeFilterFunction;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import com.huaweicloud.common.configration.dynamic.ContextProperties;

@Configuration
@ConditionalOnClass(name = {"org.springframework.web.reactive.function.client.WebClient"})
@ConditionalOnProperty(value = ContextProperties.WEBCLIENT_CONTEXT_ENABLED,
    havingValue = "true", matchIfMissing = true)
public class WebClientConfiguration {
  @Bean
  @Primary
  public WebClient.Builder webClientBuilder(List<ExchangeFilterFunction> exchangeFilterFunctions) {
    List<ExchangeFilterFunction> resultList = new ArrayList<>();
    ExchangeFilterFunction loadBalancerFunction = null;
    for (ExchangeFilterFunction function : exchangeFilterFunctions) {
      // use DeferringLoadBalancerExchangeFilterFunction if exists
      if (function instanceof ReactorLoadBalancerExchangeFilterFunction) {
        if (loadBalancerFunction == null) {
          loadBalancerFunction = function;
        }
        continue;
      }
      if (function instanceof DeferringLoadBalancerExchangeFilterFunction) {
        loadBalancerFunction = function;
        continue;
      }
      if (function instanceof Ordered) {
        resultList.add(function);
      } else {
        resultList.add(new OrderedExchangeFilterFunction(function));
      }
    }
    if (loadBalancerFunction != null) {
      resultList.add(new OrderedExchangeFilterFunction(loadBalancerFunction));
    }
    resultList.sort(Comparator.comparingInt(a -> ((Ordered) a).getOrder()));

    return WebClient.builder().filters(allFilters -> {
      allFilters.addAll(resultList);
    });
  }

  @Bean
  public ExchangeFilterFunction webClientMetricsExchangeFilterFunction() {
    return new WebClientMetricsExchangeFilterFunction();
  }

  @Bean
  public ExchangeFilterFunction serializeContextExchangeFilterFunction() {
    return new SerializeContextExchangeFilterFunction();
  }
}
