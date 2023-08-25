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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;

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
  public boolean isAllowed(String serviceId, String instanceId, Map<String, String> requestMap) {
    return checkAllow(serviceId, requestMap) && !checkDeny(serviceId, requestMap);
  }

  private boolean checkDeny(String serviceId, Map<String, String> requestMap) {
    if (securityPolicyProperties.matchDeny(serviceId, requestMap.get("uri"), requestMap.get("method"))) {
      // Tolerance mode, black policy match allow passing
      if ("permissive".equals(securityPolicyProperties.getMode())) {
        //TODO sending alarm message
        LOGGER.info("permissive model deny alarm message, consumer id={}, method={}, uri={}", serviceId,
            requestMap.get("uri"), requestMap.get("method"));
        return false;
      } else {
        return true;
      }
    } else {
      return false;
    }
  }

  private boolean checkAllow(String serviceId, Map<String, String> requestMap) {
    if (securityPolicyProperties.matchAllow(serviceId, requestMap.get("uri"), requestMap.get("method"))) {
      return true;
    } else {
      // Tolerance mode, white policy not match allow passing
      if ("permissive".equals(securityPolicyProperties.getMode())) {
        //TODO sending alarm message
        LOGGER.info("permissive model allow alarm message, consumer id={}, method={}, uri={}", serviceId,
            requestMap.get("uri"), requestMap.get("method"));
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
