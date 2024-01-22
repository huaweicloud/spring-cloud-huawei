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

package com.huaweicloud.governance.authentication;

import java.util.List;

import org.springframework.core.env.Environment;

import com.huaweicloud.common.adapters.webmvc.PreHandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ProviderAuthPreHandlerInterceptor implements PreHandlerInterceptor {

  private final RSAProviderTokenManager authenticationTokenManager;

  public ProviderAuthPreHandlerInterceptor(List<AccessController> accessControllers, Environment environment,
      AuthenticationAdapter authenticationAdapter) {
    authenticationTokenManager = new RSAProviderTokenManager(accessControllers, environment, authenticationAdapter);
  }

  @Override
  public boolean handle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (!authenticationTokenManager.checkUriWhitelist(request.getRequestURI())) {
      authenticationTokenManager.valid(request);
    }
    return true;
  }
}
