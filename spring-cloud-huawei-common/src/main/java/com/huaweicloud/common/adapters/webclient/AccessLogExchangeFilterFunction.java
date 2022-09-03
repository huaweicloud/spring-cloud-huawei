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

package com.huaweicloud.common.adapters.webclient;

import org.springframework.core.Ordered;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.huaweicloud.common.access.AccessLogLogger;
import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import reactor.core.publisher.Mono;

public class AccessLogExchangeFilterFunction implements ExchangeFilterFunction, Ordered {
  private final ContextProperties contextProperties;

  private final AccessLogLogger accessLogLogger;

  public AccessLogExchangeFilterFunction(
      ContextProperties contextProperties, AccessLogLogger accessLogLogger) {
    this.contextProperties = contextProperties;
    this.accessLogLogger = accessLogLogger;
  }

  @Override
  public int getOrder() {
    // after RetryExchangeFilterFunction
    return 10;
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    if (!contextProperties.isEnableTraceInfo()) {
      return next.exchange(request);
    }

    InvocationContext context = request.attribute(InvocationContextHolder.ATTRIBUTE_KEY).isPresent() ?
        (InvocationContext) request.attribute(InvocationContextHolder.ATTRIBUTE_KEY).get() : new InvocationContext();
    String url = request.url().getPath();
    String target = request.url().getHost() + ":" + request.url().getPort();
    accessLogLogger.log(context,
        "WebClient start request", url,
        null, target, 0, 0);

    long begin = System.currentTimeMillis();
    return next.exchange(request).doOnSuccess(response -> {
      accessLogLogger.log(context, "WebClient finish request", url,
          null, target, response.rawStatusCode(),
          System.currentTimeMillis() - begin);
    }).doOnError(error -> {
      accessLogLogger.log(context, "WebClient finish request(" + error.getClass().getName() + ")", url,
          null, target, -1,
          System.currentTimeMillis() - begin);
    });
  }
}
