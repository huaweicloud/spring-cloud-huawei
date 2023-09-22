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
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

public class RSAProviderTokenManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(RSAProviderTokenManager.class);

  private final List<AccessController> accessControllers;

  private final Environment environment;

  public RSAProviderTokenManager(List<AccessController> accessControllers, Environment environment) {
    this.accessControllers = accessControllers;
    this.environment = environment;
  }

  public void valid(String token, Map<String, String> requestMap) throws Exception {
    try {
      RsaAuthenticationToken rsaToken = null;
      String serviceName = "";
      if (environment.getProperty(Const.AUTH_TOKEN_CHECK_ENABLED, boolean.class, true)) {
        rsaToken = RSATokenCheckUtils.checkTokenInfo(token);
      } else {
        serviceName = requestMap.get(Const.AUTH_SERVICE_NAME);
      }
      boolean isAllow;
      for (AccessController accessController : accessControllers) {
        if (rsaToken != null) {
          requestMap.put(Const.AUTH_SERVICE_ID, rsaToken.getServiceId());
          requestMap.put(Const.AUTH_INSTANCE_ID, rsaToken.getInstanceId());
          if (RSATokenCheckUtils.validTokenInfo(rsaToken,
              accessController.getPublicKeyFromInstance(rsaToken.getInstanceId(), rsaToken.getServiceId()))) {
            isAllow = accessController.isAllowed(requestMap, serviceName);
          } else {
            LOGGER.error("token is expired, restart service.");
            throw new UnAuthorizedException("UNAUTHORIZED.");
          }
        } else {
          isAllow = accessController.isAllowed(requestMap, serviceName);
        }
        if (!isAllow) {
          throw new UnAuthorizedException(accessController.interceptMessage());
        }
      }
    } catch(Exception e) {
      LOGGER.error("verify error", e);
      throw e;
    }
  }
}
