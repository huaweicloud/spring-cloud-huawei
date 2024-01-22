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

import java.util.Map;

import org.springframework.cloud.client.ServiceInstance;

import com.huaweicloud.router.client.loadbalancer.CanaryFilterAdapter;
import com.huaweicloud.servicecomb.discovery.client.model.ServiceCombServiceInstance;

public class ServiceCombCanaryFilterAdapter implements CanaryFilterAdapter {

  public static final String VERSION = "version";

  @Override
  public String getVersion(ServiceInstance serviceInstance) {
    ServiceCombServiceInstance serviceCombServiceInstance = (ServiceCombServiceInstance) serviceInstance;
    return serviceCombServiceInstance.getMicroserviceInstance().getVersion();
  }

  @Override
  public String getServiceName(ServiceInstance serviceInstance) {
    return serviceInstance.getServiceId();
  }

  @Override
  public Map<String, String> getProperties(ServiceInstance serviceInstance) {
    return serviceInstance.getMetadata();
  }
}
