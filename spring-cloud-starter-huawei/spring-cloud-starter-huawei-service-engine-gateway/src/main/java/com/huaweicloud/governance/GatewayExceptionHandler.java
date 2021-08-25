/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.WebProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import reactor.core.publisher.Mono;

public class GatewayExceptionHandler extends DefaultErrorWebExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GatewayExceptionHandler.class);

  public GatewayExceptionHandler(ErrorAttributes errorAttributes,
      WebProperties.Resources resource,
      ErrorProperties errorProperties,
      ApplicationContext applicationContext) {
    super(errorAttributes, resource, errorProperties, applicationContext);
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable th) {
    if (th instanceof RequestNotPermitted) {
      exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
      DataBuffer buffer = exchange.getResponse().bufferFactory().wrap("rate limited".getBytes());
      LOGGER.warn("the request is rate limit by policy : {}",
          th.getMessage());
      return exchange.getResponse().writeWith(Mono.just(buffer));
    }
    return super.handle(exchange, th);
  }

}
