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
package com.huaweicloud.governance.handler;

import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import com.huaweicloud.governance.client.track.RequestTrackContext;
import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.policy.RetryPolicy;

import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.retry.Retry;
import io.github.resilience4j.retry.RetryConfig;
import io.github.resilience4j.retry.RetryRegistry;

import java.util.List;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.HttpServerErrorException;

import feign.Response;

public class RetryHandler extends AbstractGovHandler<Retry> {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandler.class);

  /**
   * @param supplier
   * @param policy
   * @return
   */
  @Override
  public DecorateCheckedSupplier process(DecorateCheckedSupplier supplier, Policy policy) {
    Retry retry = getActuator(policy.name(), (RetryPolicy) policy, this::getRetry);
    return supplier.withRetry(retry);
  }

  @Override
  public HandlerType type() {
    return HandlerType.CLIENT;
  }

  private Retry getRetry(RetryPolicy retryPolicy) {
    List<Integer> statusList = Arrays.stream(retryPolicy.getRetryOnResponseStatus().split(","))
        .map(Integer::parseInt).collect(Collectors.toList());
    RequestTrackContext.getServerExcluder().setEnabled(!retryPolicy.isOnSame());
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(retryPolicy.getMaxAttempts())
        .retryOnResult(getPredicate(statusList))
        .retryExceptions(HttpServerErrorException.class)
        .waitDuration(Duration.ofMillis(retryPolicy.getWaitDuration()))
        .build();

    RetryRegistry registry = RetryRegistry.of(config);
    return registry.retry(retryPolicy.name());
  }

  private Predicate getPredicate(List<Integer> statusList) {
    return response -> {
      int status = 0;
      if (response instanceof ClientHttpResponse) {
        try {
          status = ((ClientHttpResponse) response).getStatusCode().value();
        } catch (IOException e) {
          LOGGER.error("unexpect error!");
        }
      }
      if (response instanceof Response) {
        status = ((Response) response).status();
      }
      return statusList.contains(status);
    };
  }
}
