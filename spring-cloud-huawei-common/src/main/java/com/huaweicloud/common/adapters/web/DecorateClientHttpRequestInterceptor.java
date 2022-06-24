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

package com.huaweicloud.common.adapters.web;

import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.event.ClosedEventListener;
import com.huaweicloud.common.event.ClosedEventProcessor;

public class DecorateClientHttpRequestInterceptor implements
    ClientHttpRequestInterceptor, Ordered {
  private static final Logger LOGGER = LoggerFactory.getLogger(DecorateClientHttpRequestInterceptor.class);

  private static final int ORDER = 10000;

  private final List<PreClientHttpRequestInterceptor> preClientHttpRequestInterceptors;

  private final List<PostClientHttpRequestInterceptor> postClientHttpRequestInterceptors;

  private final ContextProperties contextProperties;

  private volatile boolean isShutDown = false;

  public DecorateClientHttpRequestInterceptor(
      ContextProperties contextProperties,
      ClosedEventListener closedEventListener,
      List<PreClientHttpRequestInterceptor> preClientHttpRequestInterceptors,
      List<PostClientHttpRequestInterceptor> postClientHttpRequestInterceptors) {
    this.contextProperties = contextProperties;
    this.preClientHttpRequestInterceptors = preClientHttpRequestInterceptors;
    this.postClientHttpRequestInterceptors = postClientHttpRequestInterceptors;
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
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    if (isShutDown) {
      LOGGER.warn("application is shutting down, reject requests");
      return new FallbackClientHttpResponse(503, "application is shutting down");
    }

    if (preClientHttpRequestInterceptors != null) {
      preClientHttpRequestInterceptors.forEach(interceptor -> interceptor.process(request, body));
    }
    ClientHttpResponse clientHttpResponse = execution.execute(request, body);
    if (postClientHttpRequestInterceptors != null) {
      postClientHttpRequestInterceptors.forEach(interceptor -> interceptor.process(clientHttpResponse));
    }
    return clientHttpResponse;
  }

  @Override
  public int getOrder() {
    return ORDER;
  }

  private void close() {
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
}
