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

import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.core.Ordered;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.disovery.InstanceIDAdapter;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class FeignAddServiceNameContext implements RequestInterceptor, Ordered {
  private final Registration registration;

  public FeignAddServiceNameContext(Registration registration) {
    this.registration = registration;
  }

  @Override
  public void apply(RequestTemplate requestTemplate) {
    if (this.registration == null) {
      return;
    }
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    context.putContext(InvocationContext.CONTEXT_MICROSERVICE_NAME, registration.getServiceId());
    context.putContext(InvocationContext.CONTEXT_INSTANCE_ID, InstanceIDAdapter.instanceId(registration));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE + 1;
  }
}
