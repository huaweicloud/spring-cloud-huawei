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
package com.huaweicloud.servicecomb.discovery.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.huaweicloud.router.client.loadbalancer.ZoneAwareFilterAdapter;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

public class ServiceCombZoneAwareFilterAdapter implements ZoneAwareFilterAdapter {
  @Override
  public String getRegion(ServiceInstance instance) {
    ServiceCombServiceInstance serviceCombServiceInstance = (ServiceCombServiceInstance) instance;
    if (serviceCombServiceInstance.getMicroserviceInstance().getDataCenterInfo() == null) {
      return null;
    }
    return serviceCombServiceInstance.getMicroserviceInstance().getDataCenterInfo().getRegion();
  }

  @Override
  public String getAvailableZone(ServiceInstance instance) {
    ServiceCombServiceInstance serviceCombServiceInstance = (ServiceCombServiceInstance) instance;
    if (serviceCombServiceInstance.getMicroserviceInstance().getDataCenterInfo() == null) {
      return null;
    }
    return serviceCombServiceInstance.getMicroserviceInstance().getDataCenterInfo().getAvailableZone();
  }

  @Override
  public String getRegion(Registration registration) {
    ServiceCombRegistration serviceCombRegistration = (ServiceCombRegistration) registration;
    if (serviceCombRegistration.getMicroserviceInstance().getDataCenterInfo() == null) {
      return null;
    }
    return serviceCombRegistration.getMicroserviceInstance().getDataCenterInfo().getRegion();
  }

  @Override
  public String getAvailableZone(Registration registration) {
    ServiceCombRegistration serviceCombRegistration = (ServiceCombRegistration) registration;
    if (serviceCombRegistration.getMicroserviceInstance().getDataCenterInfo() == null) {
      return null;
    }
    return serviceCombRegistration.getMicroserviceInstance().getDataCenterInfo().getAvailableZone();
  }
}
