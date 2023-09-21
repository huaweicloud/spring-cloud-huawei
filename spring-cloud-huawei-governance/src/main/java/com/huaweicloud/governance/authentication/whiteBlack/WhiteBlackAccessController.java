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

package com.huaweicloud.governance.authentication.whiteBlack;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.huaweicloud.common.configration.dynamic.BlackWhiteListProperties;
import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.Const;
import com.huaweicloud.governance.authentication.RSATokenCheckUtils;
import com.huaweicloud.governance.authentication.RsaAuthenticationToken;
import com.huaweicloud.governance.authentication.UnAuthorizedException;

/**
 * Add black / white list control to service access
 */
public class WhiteBlackAccessController implements AccessController {
  private static final Logger LOGGER = LoggerFactory.getLogger(WhiteBlackAccessController.class);

  BlackWhiteListProperties blackWhiteListProperties;

  private final AuthenticationAdapter authenticationAdapter;

  private final Environment environment;

  public WhiteBlackAccessController(AuthenticationAdapter authenticationAdapter,
      BlackWhiteListProperties blackWhiteListProperties, Environment environment) {
    this.authenticationAdapter = authenticationAdapter;
    this.blackWhiteListProperties = blackWhiteListProperties;
    this.environment = environment;
  }

  @Override
  public RsaAuthenticationToken validProcess(String token, String serviceName) throws Exception {
    if (Boolean.parseBoolean(environment.getProperty(Const.AUTH_TOKEN_CHECK_ENABLED, "true"))) {
      return RSATokenCheckUtils.checkTokenInfo(token);
    } else {
      return null;
    }
  }

  @Override
  public boolean isAllowed(Map<String, String> requestMap) {
    if ((blackWhiteListProperties.getBlack().size() > 0 || blackWhiteListProperties.getWhite().size() > 0)
      && (StringUtils.isEmpty(requestMap.get(Const.AUTH_SERVICE_ID))
        || StringUtils.isEmpty(requestMap.get(Const.AUTH_INSTANCE_ID)))) {
      LOGGER.info("please set spring.cloud.servicecomb.webmvc.tokenCheckEnabled config true.");
      throw new UnAuthorizedException("UNAUTHORIZED.");
    }
    return whiteAllowed(requestMap.get(Const.AUTH_SERVICE_ID), requestMap.get(Const.AUTH_INSTANCE_ID))
        && !blackDenied(requestMap.get(Const.AUTH_SERVICE_ID), requestMap.get(Const.AUTH_INSTANCE_ID));
  }

  @Override
  public String getPublicKeyFromInstance(String instanceId, String serviceId) {
    return authenticationAdapter.getPublicKeyFromInstance(instanceId, serviceId);
  }

  @Override
  public String interceptMessage() {
    return "UNAUTHORIZED BY WHITE BLACK";
  }

  private boolean whiteAllowed(String serviceId, String instanceId) {
    if (blackWhiteListProperties == null || blackWhiteListProperties.getWhite().isEmpty()) {
      return true;
    }
    return matchFound(serviceId, instanceId, blackWhiteListProperties.getWhite());
  }

  private boolean blackDenied(String serviceId, String instanceId) {
    if (blackWhiteListProperties == null || blackWhiteListProperties.getBlack().isEmpty()) {
      return false;
    }
    return matchFound(serviceId, instanceId, blackWhiteListProperties.getBlack());
  }

  private boolean matchFound(String serviceId, String instanceId,
      List<BlackWhiteListProperties.ConfigurationItem> ruleList) {
    for (BlackWhiteListProperties.ConfigurationItem item : ruleList) {
      if (BlackWhiteListProperties.ConfigurationItem.CATEGORY_PROPERTY.equals(item.getCategory())) {
        // as Servicecomb, we support to configure properties, e.g. serviceName, appId, environment, alias, version and so on, also support key in properties.
        // as Nacos, only support key in metaData.
        if (matchMicroserviceProperties(serviceId, instanceId, item)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean matchMicroserviceProperties(String serviceId, String instanceId,
      BlackWhiteListProperties.ConfigurationItem item) {
    String propertyValue = authenticationAdapter.getPropertyValue(serviceId, instanceId, item.getPropertyName());
    if (StringUtils.isEmpty(propertyValue)) {
      return false;
    }
    return isPatternMatch(propertyValue, item.getRule());
  }

  private boolean isPatternMatch(String value, String pattern) {
    if (pattern.startsWith("*")) {
      return value.endsWith(pattern.substring(1));
    }
    if (pattern.endsWith("*")) {
      return value.startsWith(pattern.substring(0, pattern.length() - 1));
    }
    return value.equals(pattern);
  }
}
