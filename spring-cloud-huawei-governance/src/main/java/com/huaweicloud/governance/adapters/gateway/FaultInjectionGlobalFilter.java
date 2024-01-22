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

package com.huaweicloud.governance.adapters.gateway;

import org.apache.servicecomb.governance.handler.FaultInjectionHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.processor.injection.Fault;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators;
import org.apache.servicecomb.governance.processor.injection.FaultInjectionDecorators.FaultInjectionDecorateCheckedSupplier;
import org.apache.servicecomb.http.client.common.HttpUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.filter.ReactiveLoadBalancerClientFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.web.server.ServerWebExchange;

import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;

public class FaultInjectionGlobalFilter implements GlobalFilter, Ordered {
  private final FaultInjectionHandler faultInjectionHandler;

  private final Object faultObject = new Object();

  public FaultInjectionGlobalFilter(FaultInjectionHandler faultInjectionHandler) {
    this.faultInjectionHandler = faultInjectionHandler;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    GovernanceRequestExtractor governanceRequest = GatewayUtils.createConsumerGovernanceRequest(exchange);
    Fault fault = faultInjectionHandler.getActuator(governanceRequest);
    if (fault != null) {
      FaultInjectionDecorateCheckedSupplier<Object> ds =
          FaultInjectionDecorators.ofCheckedSupplier(() -> faultObject);
      ds.withFaultInjection(fault);
      try {
        Object result = ds.get();
        if (result != faultObject) {
          DataBuffer dataBuffer;
          if (result == null) {
            dataBuffer = exchange.getResponse().bufferFactory().allocateBuffer();
          } else {
            dataBuffer = exchange.getResponse().bufferFactory().allocateBuffer()
                    .write(HttpUtils.serialize(result).getBytes(StandardCharsets.UTF_8));
          }
          return exchange.getResponse().writeWith(Mono.just(dataBuffer));
        }
      } catch (Throwable e) {
        return Mono.error(e);
      }
    }

    return chain.filter(exchange);
  }

  @Override
  public int getOrder() {
    return ReactiveLoadBalancerClientFilter.LOAD_BALANCER_CLIENT_FILTER_ORDER + 10;
  }
}
