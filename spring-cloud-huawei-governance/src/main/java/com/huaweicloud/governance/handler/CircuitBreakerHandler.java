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

import com.huaweicloud.governance.policy.CircuitBreakerPolicy;
import com.huaweicloud.governance.policy.Policy;

import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig.SlidingWindowType;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.decorators.Decorators.DecorateSupplier;
import java.time.Duration;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class CircuitBreakerHandler implements GovHandler {

  @Override
  public DecorateSupplier process(DecorateSupplier supplier, Policy policy) {
    return supplier.withCircuitBreaker(getCircuitBreaker((CircuitBreakerPolicy) policy));
  }

  /**
   * 需要提供默认值，避免用户理解复杂配置，同时提供高级配置功能
   *
   * @param policy
   * @return
   */
  private CircuitBreaker getCircuitBreaker(CircuitBreakerPolicy policy) {
    CircuitBreakerConfig circuitBreakerConfig = CircuitBreakerConfig.custom()
        //熔断 失败率(请求)百分比阈值
        .failureRateThreshold(50)
        //熔断 慢请求百分比阈值
        .slowCallRateThreshold(50)
        //从开过渡到半开的等待时间
        .waitDurationInOpenState(Duration.ofMillis(1000))
        //请求时间定义
        .slowCallDurationThreshold(Duration.ofSeconds(2))
        //进入半开状态时 允许的请求数量
        .permittedNumberOfCallsInHalfOpenState(3)
        //可以达到熔断条件的请求数量下限
        .minimumNumberOfCalls(10)
        //可以选择基于时间的滑动窗口计数或者基于请求数量的滑动窗口计数
        .slidingWindowType(SlidingWindowType.TIME_BASED)
        //滑动窗口，单位可能是请求数或者秒
        .slidingWindowSize(5)
        .build();

// Create a CircuitBreakerRegistry with a custom global configuration
    CircuitBreakerRegistry circuitBreakerRegistry =
        CircuitBreakerRegistry.of(circuitBreakerConfig);

// Get or create a CircuitBreaker from the CircuitBreakerRegistry
// with a custom configuration
    CircuitBreaker circuitBreakerWithCustomConfig = circuitBreakerRegistry
        .circuitBreaker(policy.name(), circuitBreakerConfig);
    return circuitBreakerWithCustomConfig;
  }
}
