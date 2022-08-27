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

package com.huaweicloud.router.client.gateway;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.adapters.gateway.PreGlobalFilter;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.util.HeaderUtil;
import com.huaweicloud.router.client.RouterConstant;

public class RouterContextPreGlobalFilter implements PreGlobalFilter {
  @Override
  public void process(ServerWebExchange exchange) {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    if (context.getContext(RouterConstant.CONTEXT_HEADER) == null) {
      Map<String, String> routerContext = new HashMap<>();
      HttpHeaders headers = exchange.getRequest().getHeaders();
      for (String header : headers.keySet()) {
        routerContext.put(header, headers.getFirst(header));
      }
      context.putContext(RouterConstant.CONTEXT_HEADER, HeaderUtil.serialize(routerContext));
    }
  }
}
