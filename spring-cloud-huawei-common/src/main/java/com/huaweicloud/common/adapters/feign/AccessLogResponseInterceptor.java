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

package com.huaweicloud.common.adapters.feign;

import java.io.IOException;

import com.huaweicloud.common.access.AccessLogLogger;
import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContextHolder;

import feign.InvocationContext;
import feign.ResponseInterceptor;

public class AccessLogResponseInterceptor implements ResponseInterceptor {
  private final ContextProperties contextProperties;

  private final AccessLogLogger accessLogLogger;

  public AccessLogResponseInterceptor(ContextProperties contextProperties,
      AccessLogLogger accessLogLogger) {
    this.contextProperties = contextProperties;
    this.accessLogLogger = accessLogLogger;
  }

  @Override
  public Object aroundDecode(InvocationContext invocationContext) throws IOException {
    if (!contextProperties.isEnableTraceInfo()) {
      return invocationContext.proceed();
    }
    Object result;
    com.huaweicloud.common.context.InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    long begin = System.currentTimeMillis();
    try {
      result = invocationContext.proceed();
    } finally {
      accessLogLogger.log(context, "Feign finish request",
          invocationContext.response().request().url(),
          context.getContext(com.huaweicloud.common.context.InvocationContext.CONTEXT_MICROSERVICE_NAME),
          null,
          invocationContext.response().status(),
          System.currentTimeMillis() - begin);
    }
    return result;
  }
}
