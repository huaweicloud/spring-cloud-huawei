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

import io.github.resilience4j.decorators.Decorators.DecorateSupplier;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * resilience4j 采用类似令牌桶的思想，使用限流首先要理解其原理
 * 每隔limitRefreshPeriod的时间会加入limitForPeriod个新许可
 * 如果获取不到新的许可(已经触发限流)，当前线程会park，最多等待timeoutDuration的时间
 * 采用默认单位为ms
 *
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class RateLimitingHandler implements GovHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(RateLimitingHandler.class);

  private Map<String, RateLimiter> map = new HashMap<>();

  @Override
  public DecorateSupplier process(DecorateSupplier supplier, Policy policy) {
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
      RateLimiterConfig config = RateLimiterConfig.custom()
          .limitRefreshPeriod(Duration.ofMillis(policy.getLimitRefreshPeriod()))
          .limitForPeriod(policy.getLimitForPeriod())
          .timeoutDuration(Duration.ofMillis(policy.getTimeoutDuration()))
          .build();
      RateLimiterRegistry rateLimiterRegistry = RateLimiterRegistry.of(config);
      limiter = rateLimiterRegistry.rateLimiter(policy.name());
      map.put(policy.name(), limiter);
    }
    return limiter;
  }
}
