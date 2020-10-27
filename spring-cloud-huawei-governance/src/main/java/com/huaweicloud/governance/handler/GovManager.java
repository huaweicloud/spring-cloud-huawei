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
import com.huaweicloud.governance.policy.RetryPolicy;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateSupplier;
import io.vavr.control.Try;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class GovManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(GovManager.class);

  /**
   * 对于限流、熔断、等不同的治理
   */
  @Autowired
  Map<String, GovHandler> handlers;

  @Autowired
  RetryHandler retryHandler;

  public Object processServer(List<Policy> policies, Supplier supplier) {
    DecorateSupplier ds = Decorators.ofSupplier(supplier);
    for (Policy policy : policies) {
      if (!policy.legal() || handlers.get(policy.handler()) == null) {
        LOGGER.warn("policy {} is not legal, will skip.", policy.name());
        continue;
      }
      ds = handlers.get(policy.handler()).process(ds, policy);
    }
    return Try.ofSupplier(ds.decorate())
        .recover(throwable -> {
          throw (RuntimeException) throwable;
        }).get();
  }

  public Object processClient(List<Policy> policies, Supplier supplier) {
    DecorateSupplier ds = Decorators.ofSupplier(supplier);
    for (Policy policy : policies) {
      if (!policy.legal() || retryHandler == null) {
        LOGGER.warn("policy {} is not legal, will skip.", policy.name());
        continue;
      }
      if (policy instanceof RetryPolicy) {
        ds = retryHandler.process(ds, policy);
        break;
      }
    }
    return Try.ofSupplier(ds.decorate())
        .recover(throwable -> {
          throw (RuntimeException) throwable;
        }).get();
  }
}
