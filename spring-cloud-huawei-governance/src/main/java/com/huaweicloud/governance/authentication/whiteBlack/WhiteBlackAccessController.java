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

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.huaweicloud.common.configration.dynamic.BlackWhiteListProperties;
import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthRequestExtractor;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.MatcherUtils;
import com.huaweicloud.governance.authentication.UnAuthorizedException;

/**
 * Add black / white list control to service access
 */
public class WhiteBlackAccessController implements AccessController {
  private static final Logger LOGGER = LoggerFactory.getLogger(WhiteBlackAccessController.class);

  BlackWhiteListProperties blackWhiteListProperties;

  private final AuthenticationAdapter authenticationAdapter;

  public WhiteBlackAccessController(AuthenticationAdapter authenticationAdapter,
      BlackWhiteListProperties blackWhiteListProperties) {
    this.authenticationAdapter = authenticationAdapter;
    this.blackWhiteListProperties = blackWhiteListProperties;
  }

  @Override
  public boolean isAllowed(AuthRequestExtractor extractor) throws Exception {
    if ((blackWhiteListProperties.getBlack().size() > 0 || blackWhiteListProperties.getWhite().size() > 0)
      && (StringUtils.isEmpty(extractor.serviceId()) || StringUtils.isEmpty(extractor.instanceId()))) {
      LOGGER.info("please set spring.cloud.servicecomb.webmvc.tokenCheckEnabled config true.");
      throw new UnAuthorizedException("UNAUTHORIZED.");
    }
    return whiteAllowed(extractor.serviceId(), extractor.instanceId())
        && !blackDenied(extractor.serviceId(), extractor.instanceId());
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
    return MatcherUtils.isPatternMatch(propertyValue, item.getRule());
  }
}
