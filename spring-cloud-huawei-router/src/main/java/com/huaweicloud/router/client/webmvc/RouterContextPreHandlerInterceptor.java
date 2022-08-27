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

package com.huaweicloud.router.client.webmvc;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.huaweicloud.common.adapters.webmvc.PreHandlerInterceptor;
import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.util.HeaderUtil;
import com.huaweicloud.router.client.RouterConstant;

public class RouterContextPreHandlerInterceptor implements PreHandlerInterceptor {
  @Override
  public boolean handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    if (context.getContext(RouterConstant.CONTEXT_HEADER) == null) {
      Map<String, String> routerContext = new HashMap<>();
      Enumeration<String> headers = request.getHeaderNames();
      while (headers.hasMoreElements()) {
        String header = headers.nextElement();
        routerContext.put(header, request.getHeader(header));
      }
      context.putContext(RouterConstant.CONTEXT_HEADER, HeaderUtil.serialize(routerContext));
    }
    return true;
  }
}
