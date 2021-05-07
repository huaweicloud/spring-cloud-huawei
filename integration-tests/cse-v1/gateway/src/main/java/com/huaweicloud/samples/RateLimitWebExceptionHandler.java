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

package com.huaweicloud.samples;

import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ResourceProperties;
import org.springframework.boot.autoconfigure.web.reactive.error.DefaultErrorWebExceptionHandler;
import org.springframework.boot.web.reactive.error.ErrorAttributes;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ServerWebExchange;

import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import reactor.core.publisher.Mono;

public class RateLimitWebExceptionHandler extends DefaultErrorWebExceptionHandler {

  public RateLimitWebExceptionHandler(ErrorAttributes errorAttributes, ResourceProperties resourceProperties,
      ErrorProperties errorProperties, ApplicationContext applicationContext) {
    super(errorAttributes, resourceProperties, errorProperties, applicationContext);
  }

  @Override
  public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
    if (ex instanceof RequestNotPermitted) {
      exchange.getResponse().setStatusCode(HttpStatus.TOO_MANY_REQUESTS);
      DataBuffer buffer = exchange.getResponse().bufferFactory().wrap("rate limited".getBytes());
      return exchange.getResponse().writeWith(Mono.just(buffer));
    } else {
      return super.handle(exchange, ex);
    }
  }
}
