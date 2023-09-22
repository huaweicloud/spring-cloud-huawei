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

package com.huaweicloud.governance.authentication;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.core.env.Environment;

import com.huaweicloud.common.adapters.webmvc.PreHandlerInterceptor;
import com.huaweicloud.common.context.InvocationContextHolder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProviderAuthPreHandlerInterceptor implements PreHandlerInterceptor {

  private final RSAProviderTokenManager authenticationTokenManager;

  public ProviderAuthPreHandlerInterceptor(List<AccessController> accessControllers, Environment environment) {
    authenticationTokenManager = new RSAProviderTokenManager(accessControllers, environment);
  }

  @Override
  public boolean handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    String token = InvocationContextHolder.getOrCreateInvocationContext().getContext(Const.AUTH_TOKEN);
    String serviceName = request.getHeader(Const.AUTH_SERVICE_NAME);
    if (StringUtils.isEmpty(serviceName)) {
      serviceName = InvocationContextHolder.getOrCreateInvocationContext().getContext(Const.AUTH_SERVICE_NAME);
    }
    Map<String, String> requestMap = new HashMap<>();
    requestMap.put(Const.AUTH_URI, request.getRequestURI());
    requestMap.put(Const.AUTH_METHOD, request.getMethod());
    requestMap.put(Const.AUTH_SERVICE_NAME, serviceName);
    authenticationTokenManager.valid(token, requestMap);
    return true;
  }
}
