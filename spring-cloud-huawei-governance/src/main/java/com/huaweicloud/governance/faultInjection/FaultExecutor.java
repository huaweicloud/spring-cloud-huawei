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

package com.huaweicloud.governance.faultInjection;

import java.util.concurrent.atomic.AtomicLong;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.apache.servicecomb.injection.Fault;
import org.apache.servicecomb.injection.FaultInjectionConst;
import org.apache.servicecomb.injection.FaultInjectionException;
import org.apache.servicecomb.injection.FaultInjectionUtil;
import org.apache.servicecomb.injection.FaultParam;
import org.apache.servicecomb.injection.FaultResponse;
import org.apache.servicecomb.injection.Sleepable;

import static org.apache.servicecomb.injection.AbortFault.ABORTED_ERROR_MSG;

/**
 * Implements the fault feature execution one after other.
 */
public class FaultExecutor {

  public static void execute(GovernanceRequest governanceRequest, Fault fault) {
    execute(governanceRequest, fault, null);
  }

  public static void execute(GovernanceRequest governanceRequest,
      Fault fault, Sleepable sleepable) {
    if (fault != null) {
      FaultParam param = initFaultParam(getInjectFaultKey(governanceRequest), sleepable);
      if (fault.injectFault(param)) {
        if (FaultInjectionConst.FALLBACK_THROWEXCEPTION.equals(fault.getPolicy().getFallbackType())) {
          throw new FaultInjectionException(
              FaultResponse.createFail(fault.getPolicy().getErrorCode(), ABORTED_ERROR_MSG));
        }
      }
    }
  }

  private static FaultParam initFaultParam(String key, Sleepable sleepable) {
    AtomicLong reqCount = FaultInjectionUtil.getOperMetTotalReq(key);
    // increment the request count here after checking the delay/abort condition.
    long reqCountCurrent = reqCount.getAndIncrement();

    FaultParam param = new FaultParam(reqCountCurrent);
    if (sleepable != null) {
      param.setSleepable(sleepable);
    }
    return param;
  }

  private static String getInjectFaultKey(GovernanceRequest governanceRequest) {
    return governanceRequest.getUri();
  }
}
