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
package com.huaweicloud.governance.authentication.securityPolicy;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.Const;
import com.huaweicloud.governance.authentication.RSATokenCheckUtils;
import com.huaweicloud.governance.authentication.RsaAuthenticationToken;
import com.huaweicloud.governance.authentication.UnAuthorizedException;

/**
 * Add security policy list control to service access
 */
public class SecurityPolicyAccessController implements AccessController {
  private static final Logger LOGGER = LoggerFactory.getLogger(SecurityPolicyAccessController.class);

  private SecurityPolicyProperties securityPolicyProperties;

  private final AuthenticationAdapter authenticationAdapter;

  public SecurityPolicyAccessController(AuthenticationAdapter authenticationAdapter,
      SecurityPolicyProperties securityPolicyProperties) {
    this.authenticationAdapter = authenticationAdapter;
    this.securityPolicyProperties = securityPolicyProperties;
  }

  @Override
  public void valid(String token, Map<String, String> requestMap) throws Exception {
    RsaAuthenticationToken rsaToken = checkTokenInfoOrServiceName(token);
    boolean isAllow;
    if (rsaToken == null) {
      if (StringUtils.isEmpty(requestMap.get(Const.AUTH_SERVICE_ID))) {
        LOGGER.info("consumer has no serviceName info in header, please set it for authentication");
        throw new UnAuthorizedException("UNAUTHORIZED.");
      }
      isAllow = isAllowed(requestMap.get(Const.AUTH_SERVICE_ID), requestMap);
    } else {
      if (RSATokenCheckUtils.validTokenInfo(rsaToken,getPublicKeyFromInstance(rsaToken.getInstanceId(),
          rsaToken.getServiceId()))) {
        isAllow = isAllowed(rsaToken.getServiceId(), requestMap);
      } else {
        LOGGER.error("token is expired, restart service.");
        throw new UnAuthorizedException("UNAUTHORIZED.");
      }
    }
    if (!isAllow) {
      throw new UnAuthorizedException(interceptMessage());
    }
  }

  protected boolean isAllowed(String serviceId, Map<String, String> requestMap) {
    return checkAllow(serviceId, requestMap) && !checkDeny(serviceId, requestMap);
  }

  private boolean checkDeny(String serviceId, Map<String, String> requestMap) {
    if (securityPolicyProperties.matchDeny(serviceId, requestMap.get(Const.AUTH_URI),
        requestMap.get(Const.AUTH_METHOD))) {
      // permissive mode, black policy match allow passing
      if ("permissive".equals(securityPolicyProperties.getMode())) {
        LOGGER.info("[autoauthz unauthorized request] consumer={}, provider={}, path={}, method={}, timestamp={}",
            serviceId, securityPolicyProperties.getProvider(), requestMap.get(Const.AUTH_URI),
            requestMap.get(Const.AUTH_METHOD), System.currentTimeMillis());
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean checkAllow(String serviceId, Map<String, String> requestMap) {
    if (securityPolicyProperties.matchAllow(serviceId, requestMap.get(Const.AUTH_URI),
        requestMap.get(Const.AUTH_METHOD))) {
      return true;
    } else {
      // permissive mode, white policy not match allow passing
      if ("permissive".equals(securityPolicyProperties.getMode())) {
        LOGGER.info("[autoauthz unauthorized request] consumer={}, provider={}, path={}, method={}, timestamp={}",
            serviceId, securityPolicyProperties.getProvider(), requestMap.get(Const.AUTH_URI),
            requestMap.get(Const.AUTH_METHOD), System.currentTimeMillis());
        return true;
      } else {
        return false;
      }
    }
  }

  private String getPublicKeyFromInstance(String instanceId, String serviceId) {
    return authenticationAdapter.getPublicKeyFromInstance(instanceId, serviceId);
  }

  private String interceptMessage() {
    return "UNAUTHORIZED BY SECURITY POLICY";
  }

  private RsaAuthenticationToken checkTokenInfoOrServiceName(String token) throws Exception {
    if (securityPolicyProperties.isTokenCheckEnabled()) {
      return RSATokenCheckUtils.checkTokenInfo(token);
    }
    return null;
  }
}
