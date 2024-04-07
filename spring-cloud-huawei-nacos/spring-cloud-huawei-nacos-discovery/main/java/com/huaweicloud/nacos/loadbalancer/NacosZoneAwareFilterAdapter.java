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
package com.huaweicloud.nacos.loadbalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.huaweicloud.router.client.loadbalancer.ZoneAwareFilterAdapter;

public class NacosZoneAwareFilterAdapter implements ZoneAwareFilterAdapter {
  private static final String REGION = "region";

  private static final String ZONE = "zone";

  @Override
  public String getRegion(ServiceInstance instance) {
    return instance.getMetadata().getOrDefault(REGION, "0.0.1");
  }

  @Override
  public String getAvailableZone(ServiceInstance instance) {
    return instance.getMetadata().getOrDefault(ZONE, "0.0.1");
  }

  @Override
  public String getRegion(Registration registration) {
    return registration.getMetadata().getOrDefault(REGION, "0.0.1");
  }

  @Override
  public String getAvailableZone(Registration registration) {
    return registration.getMetadata().getOrDefault(ZONE, "0.0.1");
  }
}
