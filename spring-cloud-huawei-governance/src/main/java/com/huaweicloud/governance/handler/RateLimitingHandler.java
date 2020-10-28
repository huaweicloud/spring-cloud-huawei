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

import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.policy.RateLimitingPolicy;

import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class RateLimitingHandler implements GovHandler {

  private Map<String, RateLimiter> map = new HashMap<>();

  @Override
  public DecorateCheckedSupplier process(DecorateCheckedSupplier supplier, Policy policy) {
    return supplier.withRateLimiter(getRateLimiter((RateLimitingPolicy) policy));
  }

  /**
   * todo: 考虑并发
   * @param policy
   * @return
   */
  private RateLimiter getRateLimiter(RateLimitingPolicy policy) {
    RateLimiter limiter = map.get(policy.name());
    if (limiter == null) {
      RateLimiterConfig config;
      if (policy.getRate() != null) {
        config = RateLimiterConfig.custom()
            .limitForPeriod(policy.getRate())
            .limitRefreshPeriod(Duration.ofMillis(RateLimitingPolicy.DEFAULT_LIMIT_REFRESH_PERIOD))
            .timeoutDuration(Duration.ofMillis(RateLimitingPolicy.DEFAULT_TIMEOUT_DURATION))
            .build();
      } else {
        config = RateLimiterConfig.custom()
            .limitForPeriod(policy.getLimitForPeriod())
            .limitRefreshPeriod(Duration.ofMillis(policy.getLimitRefreshPeriod()))
            .timeoutDuration(Duration.ofMillis(policy.getTimeoutDuration()))
            .build();
      }
      RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
      limiter = rateLimiterRegistry.rateLimiter(policy.name());
      map.put(policy.name(), limiter);
    }
    return limiter;
  }
}
