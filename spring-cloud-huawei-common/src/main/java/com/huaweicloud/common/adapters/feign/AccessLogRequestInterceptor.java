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

import org.springframework.core.Ordered;

import com.huaweicloud.common.access.AccessLogLogger;
import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class AccessLogRequestInterceptor implements RequestInterceptor, Ordered {
  private final ContextProperties contextProperties;

  private final AccessLogLogger accessLogLogger;

  public AccessLogRequestInterceptor(ContextProperties contextProperties,
      AccessLogLogger accessLogLogger) {
    this.contextProperties = contextProperties;
    this.accessLogLogger = accessLogLogger;
  }

  @Override
  public void apply(RequestTemplate requestTemplate) {
    if (!contextProperties.isEnableTraceInfo()) {
      return;
    }

    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    accessLogLogger.log(context, "Feign start request", requestTemplate.feignTarget().name() +
            requestTemplate.request().url(),
        context.getContext(InvocationContext.CONTEXT_MICROSERVICE_NAME), null, 0, 0);
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 2;
  }
}
