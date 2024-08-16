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
package com.huaweicloud.governance.adapters.webclient;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.huaweicloud.governance.adapters.loadbalancer.RetryContext;

import reactor.core.publisher.Mono;

public class ServiceInfoLoggerExchangeFilterFunction implements ExchangeFilterFunction, Ordered {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceInfoLoggerExchangeFilterFunction.class);

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return next.exchange(request).map(response -> logServiceInfo(response, request));
  }

  private ClientResponse logServiceInfo(ClientResponse response, ClientRequest request) {
    if (response.statusCode().value() != 200) {
      Optional<Object> invocationContext = request.attribute(RetryContext.RETRY_SERVICE_INSTANCE);
      if (invocationContext.isPresent() && invocationContext.get() instanceof ServiceInstance instance) {
        LOGGER.error("request >>>>>>>>>>>>>> service {}[{}:{}] failed", instance.getServiceId(), instance.getHost(),
            instance.getPort());
      }
    }
    return response;
  }

  @Override
  public int getOrder() {
    return Integer.MAX_VALUE;
  }
}
