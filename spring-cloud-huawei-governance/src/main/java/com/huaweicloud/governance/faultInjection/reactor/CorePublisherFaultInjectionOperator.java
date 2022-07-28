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

package com.huaweicloud.governance.faultInjection.reactor;

import java.time.Duration;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.injection.Fault;
import org.apache.servicecomb.injection.FaultInjectionException;

import com.huaweicloud.governance.faultInjection.FaultExecutor;

import reactor.core.CorePublisher;
import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Operators;

public class CorePublisherFaultInjectionOperator<T> {

  private final CorePublisher<? extends T> source;

  private final GovernanceRequest governanceRequest;

  private final Fault fault;

  private boolean executed = false;

  CorePublisherFaultInjectionOperator(CorePublisher<? extends T> source, GovernanceRequest governanceRequest,
      Fault fault) {
    this.source = source;
    this.governanceRequest = governanceRequest;
    this.fault = fault;
  }

  void subscribe(CoreSubscriber<? super T> actual) {
    try {
      FaultExecutor.execute(governanceRequest,
          fault,
          delay -> delaySubscription(actual, delay));

      if (!executed) {
        source.subscribe(new FaultInjectionSubscriber<>(actual));
      }
    } catch (FaultInjectionException e) {
      Operators.error(actual, e);
    }
  }

  private void delaySubscription(CoreSubscriber<? super T> actual, long waitDuration) {
    executed = true;
    Mono.delay(Duration.ofMillis(waitDuration))
        .subscribe(delay -> source.subscribe(
            new FaultInjectionSubscriber<>(actual)));
  }
}
