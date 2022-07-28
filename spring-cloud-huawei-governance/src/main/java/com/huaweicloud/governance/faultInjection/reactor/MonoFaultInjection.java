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

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.injection.Fault;

import reactor.core.CoreSubscriber;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoOperator;

public class MonoFaultInjection<T> extends MonoOperator<T, T> {

  private final CorePublisherFaultInjectionOperator<T> operator;

  MonoFaultInjection(Mono<? extends T> source, GovernanceRequest governanceRequest, Fault fault) {
    super(source);
    this.operator = new CorePublisherFaultInjectionOperator<T>(source, governanceRequest, fault);
  }

  @Override
  public void subscribe(CoreSubscriber<? super T> actual) {
    operator.subscribe(actual);
  }
}
