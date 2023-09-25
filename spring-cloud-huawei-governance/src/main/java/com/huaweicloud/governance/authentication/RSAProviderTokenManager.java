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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class RSAProviderTokenManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RSAProviderTokenManager.class);

  private final List<AccessController> accessControllers;

  private final Environment environment;

  private final AuthenticationAdapter authenticationAdapter;

  public RSAProviderTokenManager(List<AccessController> accessControllers, Environment environment,
      AuthenticationAdapter authenticationAdapter) {
    this.accessControllers = accessControllers;
    this.environment = environment;
    this.authenticationAdapter = authenticationAdapter;
  }

  public void valid(HttpServletRequest request) throws Exception {
    try {
      RsaAuthenticationToken rsaToken = null;
      if (environment.getProperty(Const.AUTH_TOKEN_CHECK_ENABLED, boolean.class, true)) {
        rsaToken = RSATokenCheckUtils.checkTokenInfo(request, authenticationAdapter);
      }
      AuthRequestExtractor extractor;
      if (rsaToken != null) {
        extractor = AuthRequestExtractorUtils.createAuthRequestExtractor(request, rsaToken.getServiceId(),
            rsaToken.getInstanceId());
      } else {
        extractor = AuthRequestExtractorUtils.createAuthRequestExtractor(request, "", "");
      }
      for (AccessController accessController : accessControllers) {
        if (!accessController.isAllowed(extractor)) {
          throw new UnAuthorizedException(accessController.interceptMessage());
        }
      }
    } catch(Exception e) {
      LOGGER.error("verify error", e);
      throw e;
    }
  }
}
