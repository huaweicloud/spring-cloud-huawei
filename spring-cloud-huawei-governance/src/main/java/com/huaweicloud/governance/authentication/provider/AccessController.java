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

package com.huaweicloud.governance.authentication.provider;

import com.huaweicloud.common.configration.dynamic.BlackWhiteListProperties;
import com.huaweicloud.common.governance.GovernaceServiceInstance;

import java.util.List;

import org.apache.commons.lang.StringUtils;

/**
 * Add black / white list control to service access
 */
public class AccessController {
  BlackWhiteListProperties blackWhiteListProperties;

  private final GovernaceServiceInstance instanceService;

  public AccessController(GovernaceServiceInstance instanceService,
      BlackWhiteListProperties blackWhiteListProperties) {
    this.instanceService = instanceService;
    this.blackWhiteListProperties = blackWhiteListProperties;
  }

  public boolean isAllowed(String serviceId, String instanceId) {
    return whiteAllowed(serviceId, instanceId) && !blackDenied(serviceId, instanceId);
  }

  public String getPublicKeyFromInstance(String instanceId, String serviceId) {
    return instanceService.getPublicKeyFromInstance(instanceId, serviceId);
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
    String propertyValue = instanceService.getPropertyValue(serviceId, instanceId, item.getPropertyName());
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