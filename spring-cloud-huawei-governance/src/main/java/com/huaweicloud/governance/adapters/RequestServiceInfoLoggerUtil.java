/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.governance.adapters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.governance.GovernanceConst;

public class RequestServiceInfoLoggerUtil {
  private static final Logger LOGGER = LoggerFactory.getLogger(RequestServiceInfoLoggerUtil.class);

  public static void logServiceInfo(InvocationContext context, Throwable e) {
    if (context != null && context.getLocalContext(GovernanceConst.CONTEXT_CURRENT_INSTANCE) != null) {
      ServiceInstance instance = context.getLocalContext(GovernanceConst.CONTEXT_CURRENT_INSTANCE);
      LOGGER.error("request >>>>>>>>>>>>>> service {}[{}:{}] failed", instance.getServiceId(), instance.getHost(),
          instance.getPort(), e);
    }
  }
}
