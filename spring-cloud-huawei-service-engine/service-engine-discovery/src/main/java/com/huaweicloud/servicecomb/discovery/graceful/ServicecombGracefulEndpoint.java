/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.servicecomb.discovery.graceful;

import javax.annotation.Nullable;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombServiceRegistry;

@Endpoint(id = "servicecomb-service-registry")
public class ServicecombGracefulEndpoint {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServicecombGracefulEndpoint.class);

  private final ServiceCombServiceRegistry serviceCombServiceRegistry;

  private final ServiceCombRegistration serviceCombRegistration;

  public ServicecombGracefulEndpoint(ServiceCombServiceRegistry serviceCombServiceRegistry,
      ServiceCombRegistration serviceCombRegistration) {
    this.serviceCombServiceRegistry = serviceCombServiceRegistry;
    this.serviceCombRegistration = serviceCombRegistration;
  }

  @WriteOperation
  public void gracefulUpperAndDown(@Nullable String status) {
    if (StringUtils.isEmpty(status)
        || (!GovernanceProperties.GRASEFUL_STATUS_UPPER.equalsIgnoreCase(status)
        && !GovernanceProperties.GRASEFUL_STATUS_DOWN.equalsIgnoreCase(status))) {
      LOGGER.warn("status input " + status + " is not a valid value.");
      return;
    }
    serviceCombServiceRegistry.setStatus(serviceCombRegistration, status.toUpperCase());
  }
}
