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

package com.huaweicloud.common.adapters.webflux;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.event.ClosedEventListener;
import com.huaweicloud.common.event.ClosedEventProcessor;

import reactor.core.publisher.Mono;

public class ShutdownHookWebFilter implements OrderedWebFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(ShutdownHookWebFilter.class);

  private final ContextProperties contextProperties;

  private volatile boolean isShutDown = false;

  public ShutdownHookWebFilter(
      ContextProperties contextProperties,
      ClosedEventListener closedEventListener) {
    this.contextProperties = contextProperties;
    closedEventListener.addClosedEventProcessor(new ClosedEventProcessor() {
      @Override
      public void process() {
        close();
      }

      @Override
      public int getOrder() {
        return 200;
      }
    });
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    if (isShutDown) {
      LOGGER.warn("application is shutting down, reject request {}", exchange.getRequest().getURI());
      exchange.getResponse().setStatusCode(HttpStatus.SERVICE_UNAVAILABLE);
      return Mono.empty();
    }

    return chain.filter(exchange);
  }

  private void close() {
    if (isShutDown) {
      return;
    }
    LOGGER.warn("application is shutting down, rejecting requests...");
    isShutDown = true;
    if (contextProperties.getWaitTimeForShutDownInMillis() > 0) {
      try {
        LOGGER.info("wait {}ms for requests done.", contextProperties.getWaitTimeForShutDownInMillis());
        Thread.sleep(contextProperties.getWaitTimeForShutDownInMillis());
      } catch (InterruptedException e) {
        // ignore
      }
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 2;
  }
}
