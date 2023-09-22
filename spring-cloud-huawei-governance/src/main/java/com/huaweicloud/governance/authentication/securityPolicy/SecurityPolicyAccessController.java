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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthRequestExtractor;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
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
  public boolean isAllowed(AuthRequestExtractor extractor) throws Exception {
    String currentServiceName = extractor.serviceName();
    if (StringUtils.isEmpty(extractor.serviceId()) && StringUtils.isEmpty(currentServiceName)) {
      LOGGER.info("consumer has no serviceName info in header, please set it for authentication");
      throw new UnAuthorizedException("UNAUTHORIZED.");
    }
    if (StringUtils.isEmpty(currentServiceName)) {
      currentServiceName = authenticationAdapter.getServiceName(extractor.serviceId());
    }
    return checkAllowAndDeny(currentServiceName, extractor);
  }

  private boolean checkDeny(String serviceName, AuthRequestExtractor extractor) {
    if (securityPolicyProperties.matchDeny(serviceName, extractor.apiPath(), extractor.method())) {
      // permissive mode, black policy match allow passing
      if ("permissive".equals(securityPolicyProperties.getMode())) {
        LOGGER.info("[autoauthz unauthorized request] consumer={}, provider={}, path={}, method={}, timestamp={}",
            serviceName, securityPolicyProperties.getProvider(), extractor.apiPath(), extractor.method(),
            System.currentTimeMillis());
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean checkAllowAndDeny(String serviceName, AuthRequestExtractor extractor) {
    if (securityPolicyProperties.matchAllow(serviceName, extractor.apiPath(), extractor.method())) {
      return !checkDeny(serviceName, extractor);
    } else {
      // permissive mode, white policy not match allow passing
      if ("permissive".equals(securityPolicyProperties.getMode())) {
        LOGGER.info("[autoauthz unauthorized request] consumer={}, provider={}, path={}, method={}, timestamp={}",
            serviceName, securityPolicyProperties.getProvider(), extractor.apiPath(), extractor.method(),
            System.currentTimeMillis());
        return true;
      } else {
        return false;
      }
    }
  }

  @Override
  public String getPublicKeyFromInstance(String instanceId, String serviceId) {
    return authenticationAdapter.getPublicKeyFromInstance(instanceId, serviceId);
  }

  @Override
  public String interceptMessage() {
    return "UNAUTHORIZED BY SECURITY POLICY";
  }
}
