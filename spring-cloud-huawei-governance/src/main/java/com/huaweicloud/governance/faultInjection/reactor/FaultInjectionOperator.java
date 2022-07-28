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

package com.huaweicloud.governance.faultInjection.reactor;

import java.util.function.UnaryOperator;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.injection.Fault;
import org.reactivestreams.Publisher;

import io.github.resilience4j.reactor.IllegalPublisherException;
import reactor.core.publisher.Mono;

public class FaultInjectionOperator<T> implements UnaryOperator<Publisher<T>> {

  private final GovernanceRequest governanceRequest;

  private final Fault fault;

  private FaultInjectionOperator(GovernanceRequest governanceRequest, Fault fault) {
    this.governanceRequest = governanceRequest;
    this.fault = fault;
  }

  public static <T> FaultInjectionOperator<T> of(
      GovernanceRequest governanceRequest,
      Fault fault) {
    return new FaultInjectionOperator<>(governanceRequest, fault);
  }

  @Override
  public Publisher<T> apply(Publisher<T> publisher) {
    if (publisher instanceof Mono) {
      return new MonoFaultInjection<>((Mono<? extends T>) publisher, governanceRequest, fault);
    } else {
      throw new IllegalPublisherException(publisher);
    }
  }
}
