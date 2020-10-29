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
package com.huaweicloud.governance;

import com.huaweicloud.governance.handler.GovHandler;
import com.huaweicloud.governance.handler.HandlerType;
import com.huaweicloud.governance.handler.ext.ClientRecoverPolicy;
import com.huaweicloud.governance.handler.ext.ServerRecoverPolicy;
import com.huaweicloud.governance.policy.Policy;

import io.github.resilience4j.decorators.Decorators;
import io.github.resilience4j.decorators.Decorators.DecorateCheckedSupplier;
import io.vavr.CheckedFunction0;
import io.vavr.control.Try;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
public class GovManager {

  @Autowired
  Map<String, GovHandler> handlers;

  @Autowired(required = false)
  ServerRecoverPolicy serverRecoverPolicy;

  @Autowired(required = false)
  ClientRecoverPolicy clientRecoverPolicy;

  public Object processServer(List<Policy> policies, CheckedFunction0 supplier) {
    DecorateCheckedSupplier ds = Decorators.ofCheckedSupplier(supplier);
    for (Policy policy : policies) {
      if (handlers.get(policy.handler()) == null ||
          handlers.get(policy.handler()).type() == HandlerType.CLIENT) {
        continue;
      }
      ds = handlers.get(policy.handler()).process(ds, policy);
    }
    return Try.of(ds.decorate())
        .recover(throwable -> {
          if (serverRecoverPolicy == null) {
            throw (RuntimeException) throwable;
          } else {
            return serverRecoverPolicy.apply((Throwable) throwable);
          }
        }).get();
  }

  public Object processClient(List<Policy> policies, CheckedFunction0 supplier) {
    DecorateCheckedSupplier ds = Decorators.ofCheckedSupplier(supplier);
    for (Policy policy : policies) {
      if (handlers.get(policy.handler()) == null ||
          handlers.get(policy.handler()).type() == HandlerType.SERVER) {
        continue;
      }
      ds = handlers.get(policy.handler()).process(ds, policy);
    }
    return Try.of(ds.decorate())
        .recover(throwable -> {
          if (clientRecoverPolicy == null) {
            throw (RuntimeException) throwable;
          } else {
            return clientRecoverPolicy.apply((Throwable) throwable);
          }
        }).get();
  }
}
