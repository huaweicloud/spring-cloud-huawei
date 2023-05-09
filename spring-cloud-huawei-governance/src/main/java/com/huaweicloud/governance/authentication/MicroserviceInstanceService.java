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

package com.huaweicloud.governance.authentication;

import org.springframework.cloud.client.ServiceInstance;

import com.huaweicloud.governance.authentication.instance.CommonInstance;

/**
 * microservice instance service
 */
public interface MicroserviceInstanceService extends ServiceInstance {
  void setPublickey(String publicKeyEncoded);

  @Override
  String getInstanceId();

  @Override
  String getServiceId();

  String getPublicKeyFromInstance(String instanceId, String serviceId);

  String getPropertyValue(String serviceId, String instanceId, String propertyName);

  CommonInstance getMicroserviceInstance();

  CommonInstance getMicroserviceInstance(ServiceInstance serviceInstance);

  String getAvailableZone();

  String getRegion();
}
