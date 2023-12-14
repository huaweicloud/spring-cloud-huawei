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
package com.huaweicloud.crossappsample;

import com.huaweicloud.common.configration.dynamic.BlackWhiteListProperties;
import com.huaweicloud.governance.authentication.AuthHandlerBoot;
import com.huaweicloud.governance.authentication.Const;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.authentication.ProviderAuthPreHandlerInterceptor;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties;

@RestController
public class AuthController {

  private final ApplicationContext applicationContext;

  @Autowired
  public AuthController(ApplicationContext applicationContext) {
    this.applicationContext = applicationContext;
  }

  @RequestMapping("/checkToken")
  public String checkToken() {
    ProviderAuthPreHandlerInterceptor interceptor = applicationContext.getBean(ProviderAuthPreHandlerInterceptor.class);
    AuthHandlerBoot authHandlerBoot = applicationContext.getBean(AuthHandlerBoot.class);
    BlackWhiteListProperties blackWhiteListProperties = applicationContext.getBean(BlackWhiteListProperties.class);
    if (interceptor == null || authHandlerBoot == null || blackWhiteListProperties == null
        || blackWhiteListProperties.getBlack().size() != 2 || blackWhiteListProperties.getWhite().size() != 1) {
      return null;
    }

    InvocationContext invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    if (StringUtils.isEmpty(invocationContext.getContext(Const.AUTH_TOKEN))) {
      return null;
    }
    return "success";
  }

  @RequestMapping("/checkTokenSecurity")
  public String checkTokenSecurity() {
    SecurityPolicyProperties securityPolicyProperties = applicationContext.getBean(SecurityPolicyProperties.class);
    ProviderAuthPreHandlerInterceptor interceptor = applicationContext.getBean(ProviderAuthPreHandlerInterceptor.class);
    AuthHandlerBoot authHandlerBoot = applicationContext.getBean(AuthHandlerBoot.class);
    if (interceptor == null || authHandlerBoot == null || securityPolicyProperties == null
        || securityPolicyProperties.getAction().getAllow().size() != 2
        || securityPolicyProperties.getAction().getDeny().size() != 2) {
      return null;
    }

    InvocationContext invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    if (StringUtils.isEmpty(invocationContext.getContext(Const.AUTH_TOKEN))) {
      return null;
    }
    return "success";
  }

  @RequestMapping("/checkWhitelist")
  public String checkWhitelist() {
    return "success";
  }
}
