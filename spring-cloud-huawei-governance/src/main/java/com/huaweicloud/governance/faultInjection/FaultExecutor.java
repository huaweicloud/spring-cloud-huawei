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

package com.huaweicloud.governance.faultInjection;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.foundation.common.utils.SPIServiceUtils;
import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.governance.policy.FaultInjectionPolicy;
import org.apache.servicecomb.injection.Fault;
import org.apache.servicecomb.injection.FaultInjectionUtil;
import org.apache.servicecomb.injection.FaultParam;
import org.apache.servicecomb.injection.Sleepable;

import io.vertx.core.Context;
import io.vertx.core.Vertx;

/**
 * Implements the fault feature execution one after other.
 */
public class FaultExecutor {
  private static final List<Fault> faultInjectList = SPIServiceUtils.getSortedService(Fault.class);

  public static void execute(GovernanceRequest governanceRequest,
      FaultInjectionPolicy policy) {
    execute(governanceRequest, policy,null);
  }

  public static void execute(GovernanceRequest governanceRequest,
      FaultInjectionPolicy policy, Sleepable sleepable) {
    Fault fault = null;
    for (Fault f : faultInjectList) {
      if (policy.getType().equals(f.getName())) {
        fault = f;
        break;
      }
    }

    if (fault != null) {
      FaultParam param = initFaultParam(getInjectFaultKey(governanceRequest), sleepable);
      fault.injectFault(param, policy, faultResponse -> {
        if (!faultResponse.isSuccess()) {
          throw new FaultInjectionException(faultResponse);
        }
      });
    }
  }

  private static FaultParam initFaultParam(String key, Sleepable sleepable) {
    AtomicLong reqCount = FaultInjectionUtil.getOperMetTotalReq(key);
    // increment the request count here after checking the delay/abort condition.
    long reqCountCurrent = reqCount.getAndIncrement();

    FaultParam param = new FaultParam(reqCountCurrent);
    if(sleepable!=null){
      param.setSleepable(sleepable);
    }else{
      Context currentContext = Vertx.currentContext();
      if (currentContext != null && currentContext.owner() != null && currentContext.isEventLoopContext()) {
        param.setSleepable(
            (delay, sleepCallback) -> currentContext.owner().setTimer(delay, timeId -> sleepCallback.callback()));
      }
    }
    return param;
  }

  private static String getInjectFaultKey(GovernanceRequest governanceRequest) {
    return governanceRequest.getUri();
  }
}
