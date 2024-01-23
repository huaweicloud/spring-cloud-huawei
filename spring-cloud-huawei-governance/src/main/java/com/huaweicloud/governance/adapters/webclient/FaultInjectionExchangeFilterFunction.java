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

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.processor.injection.Fault;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators.FaultInjectionDecorateCheckedSupplier;
import org.apache.servicecomb.http.client.common.HttpUtils;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ExchangeFunction;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

import reactor.core.publisher.Mono;

public class FaultInjectionExchangeFilterFunction implements ExchangeFilterFunction, Ordered {
  private final Object faultObject = new Object();

  private final GovernanceProperties governanceProperties;

  private final FaultInjectionHandler faultInjectionHandler;

  public FaultInjectionExchangeFilterFunction(
      GovernanceProperties governanceProperties,
      FaultInjectionHandler faultInjectionHandler) {
    this.governanceProperties = governanceProperties;
    this.faultInjectionHandler = faultInjectionHandler;
  }

  @Override
  public int getOrder() {
    return governanceProperties.getWebclient().getFaultInjection().getOrder();
  }

  @Override
  public Mono<ClientResponse> filter(ClientRequest request, ExchangeFunction next) {
    GovernanceRequestExtractor governanceRequest = WebClientUtils.createGovernanceRequest(request);
    Fault fault = faultInjectionHandler.getActuator(governanceRequest);
    if (fault != null) {
      FaultInjectionDecorateCheckedSupplier<Object> ds =
          FaultInjectionDecorators.ofCheckedSupplier(() -> faultObject);
      ds.withFaultInjection(fault);
      try {
        Object result = ds.get();
        if (result != faultObject) {
          if (result == null) {
            return Mono.just(ClientResponse.create(HttpStatus.OK)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .build());
          }
          return Mono.just(ClientResponse.create(HttpStatus.OK)
                  .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                  .body(HttpUtils.serialize(result)).build());
        }
      } catch (Throwable e) {
        return Mono.error(e);
      }
    }

    return next.exchange(request);
  }
}
