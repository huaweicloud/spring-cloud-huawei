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

package com.huaweicloud.common.adapters.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

public class TraceIdPreGlobalFilter implements PreGlobalFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(TraceIdPreGlobalFilter.class);

  private final ContextProperties contextProperties;

  public TraceIdPreGlobalFilter(ContextProperties contextProperties) {
    this.contextProperties = contextProperties;
  }

  @Override
  public void process(ServerWebExchange exchange) {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    if (context.getContext(InvocationContext.CONTEXT_TRACE_ID) == null) {
      context.putContext(InvocationContext.CONTEXT_TRACE_ID, InvocationContext.generateTraceId());
    }
    if (contextProperties.isEnableTraceInfo()) {
      Response<ServiceInstance> response = exchange.getAttribute(
          ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
      String service = "";
      if (response != null) {
        service = response.getServer().getServiceId() + ":" + response.getServer().getHost();
      }
      LOGGER.info("receive request [{}] to service [{}]. trace id [{}]", exchange.getRequest().getURI(),
          service,
          context.getContext(InvocationContext.CONTEXT_TRACE_ID));
    }
  }
}
