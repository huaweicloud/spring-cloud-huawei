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

import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import reactor.core.publisher.Mono;

public class OrderedExchangeFilterFunction implements ExchangeFilterFunction, Ordered {
  private final ExchangeFilterFunction exchangeFilterFunction;

  public OrderedExchangeFilterFunction(
      ExchangeFilterFunction exchangeFilterFunction) {
    this.exchangeFilterFunction = exchangeFilterFunction;
  }

  @Override
  public int getOrder() {
    return 0;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    return this.exchangeFilterFunction.filter(request, next);
  }

  @Override
  public ExchangeFilterFunction andThen(ExchangeFilterFunction afterFilter) {
    return this.exchangeFilterFunction.andThen(afterFilter);
  }

  @Override
  public ExchangeFunction apply(ExchangeFunction exchange) {
    return this.exchangeFilterFunction.apply(exchange);
  }
}
