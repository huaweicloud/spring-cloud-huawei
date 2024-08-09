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
package com.huaweicloud.governance.adapters.webmvc;

import java.io.IOException;

import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.adapters.RequestServiceInfoLoggerUtil;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;

public class RequestServiceInfoLoggerFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    try {
      if (WebMvcUtils.isNotHttpServlet(request, response)) {
        chain.doFilter(request, response);
        return;
      }
      chain.doFilter(request, response);
    } catch (Throwable e) {
      RequestServiceInfoLoggerUtil.logServiceInfo(InvocationContextHolder.getOrCreateInvocationContext(), e);
      throw e;
    }
  }
}
