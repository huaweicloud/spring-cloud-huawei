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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.Predicate;

import com.huaweicloud.governance.client.track.RequestTrackContext;
import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.policy.RetryPolicy;

import io.github.resilience4j.decorators.Decorators.DecorateSupplier;
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

public class RetryHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RetryHandler.class);

  /**
   * @param supplier
   * @param policy
   * @return
   */
  public DecorateSupplier process(DecorateSupplier supplier, Policy policy) {
    RetryPolicy retryPolicy = (RetryPolicy) policy;
    List<Integer> statusList;
    if (retryPolicy.getRetryOnResponseStatus() == null) {
      statusList = new ArrayList<>();
      statusList.add(502);
    } else {
      statusList = Arrays.stream(retryPolicy.getRetryOnResponseStatus().split(","))
          .map(Integer::parseInt).collect(Collectors.toList());
    }
    RequestTrackContext.getServerExcluder().setEnabled(!retryPolicy.isOnSame());
    RetryConfig config = RetryConfig.custom()
        .maxAttempts(retryPolicy.getMaxAttempts())
        .retryOnResult(getPredicate(statusList))
        .retryExceptions(HttpServerErrorException.class)
        .waitDuration(Duration.ofMillis(0))
        .build();

    RetryRegistry registry = RetryRegistry.of(config);
    Retry retry = registry.retry(policy.name());
    return supplier.withRetry(retry);
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
