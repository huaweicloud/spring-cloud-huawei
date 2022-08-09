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

import java.beans.PropertyDescriptor;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.service.center.client.model.Microservice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.governance.authentication.Const;
import com.huaweicloud.governance.authentication.provider.BlackWhiteListProperties.ConfigurationItem;
import com.netflix.config.DynamicPropertyFactory;

/**
 * Add black / white list control to service access
 */
public class AccessController {

  private static final Logger LOG = LoggerFactory.getLogger(AccessController.class);

  BlackWhiteListProperties blackWhiteListProperties;

  public AccessController(BlackWhiteListProperties blackWhiteListProperties) {
    this.blackWhiteListProperties = blackWhiteListProperties;
  }

  public boolean isAllowed(Microservice microservice) {
    return whiteAllowed(microservice) && !blackDenied(microservice);
  }

  private boolean whiteAllowed(Microservice microservice) {
    if (blackWhiteListProperties == null || blackWhiteListProperties.getWhite().isEmpty()) {
      return true;
    }
    return matchFound(microservice, blackWhiteListProperties.getWhite());
  }

  private boolean blackDenied(Microservice microservice) {
    if (blackWhiteListProperties == null || blackWhiteListProperties.getBlack().isEmpty()) {
      return false;
    }
    return matchFound(microservice, blackWhiteListProperties.getBlack());
  }

  private boolean matchFound(Microservice microservice, List<ConfigurationItem> ruleList) {
    for (ConfigurationItem item : ruleList) {
      if (ConfigurationItem.CATEGORY_PROPERTY.equals(item.getCategory())) {
        // we support to configure properties, e.g. serviceName, appId, environment, alias, version and so on, also support key in properties.
        if (matchMicroserviceField(microservice, item) || matchMicroserviceProperties(microservice, item)) {
          return true;
        }
      }
    }
    return false;
  }

  private boolean matchMicroserviceProperties(Microservice microservice, ConfigurationItem item) {
    Map<String, String> properties = microservice.getProperties();
    for (Entry<String, String> entry : properties.entrySet()) {
      if (!entry.getKey().equals(item.getPropertyName())) {
        continue;
      }
      return isPatternMatch(entry.getValue(), item.getRule());
    }
    return false;
  }

  private boolean matchMicroserviceField(Microservice microservice, ConfigurationItem item) {
    Object fieldValue = null;
    try {
      fieldValue =
          new PropertyDescriptor(item.getPropertyName(), Microservice.class).getReadMethod().invoke(microservice);
    } catch (Exception e) {
      LOG.warn("can't find property name: {} in microservice field, will search in microservice properties.",
          item.getPropertyName());
      return false;
    }
    if (fieldValue.getClass().getName().equals(String.class.getName())) {
      return isPatternMatch((String) fieldValue, item.getRule());
    }
    return false;
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
