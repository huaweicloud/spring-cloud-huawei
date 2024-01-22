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

package com.huaweicloud.common.adapters.feign;

import org.springframework.core.Ordered;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class InvocationContextRequestInterceptor implements RequestInterceptor, Ordered {
  private final ContextProperties contextProperties;

  public InvocationContextRequestInterceptor(ContextProperties contextProperties) {
    this.contextProperties = contextProperties;
  }

  @Override
  public void apply(RequestTemplate requestTemplate) {
    if (!contextProperties.isAddTraceIdForFeign()) {
      return;
    }
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    if (context.getContext(InvocationContext.CONTEXT_TRACE_ID) == null) {
      context.putContext(InvocationContext.CONTEXT_TRACE_ID, InvocationContext.generateTraceId());
    }
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
