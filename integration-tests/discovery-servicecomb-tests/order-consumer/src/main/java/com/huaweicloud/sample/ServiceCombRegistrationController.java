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
package com.huaweicloud.sample;

import org.apache.servicecomb.service.center.client.model.Microservice;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstanceStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.huaweicloud.servicecomb.discovery.ConditionalOnServiceCombDiscoveryEnabled;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

@RestController
@ConditionalOnServiceCombDiscoveryEnabled
public class ServiceCombRegistrationController {
  private final ServiceCombRegistration serviceCombRegistration;

  @Autowired
  public ServiceCombRegistrationController(
      ServiceCombRegistration serviceCombRegistration) {
    this.serviceCombRegistration = serviceCombRegistration;
  }

  @RequestMapping("/testMicroserviceInfoCorrect")
  public boolean testMicroserviceInfoCorrect() {
    Microservice microservice = serviceCombRegistration.getMicroservice();
    assertTrue(microservice.getAppId().equals("default"));
    assertTrue(microservice.getServiceName().equals("order"));
    assertTrue(microservice.getVersion().equals("0.0.1"));
    assertTrue(microservice.getProperties().get("x-test").equals("value"));
    assertTrue(microservice.getProperties().get("x-test2").equals("value2"));

    MicroserviceInstance microserviceInstance = serviceCombRegistration.getMicroserviceInstance();
    assertTrue(microserviceInstance.getProperties().get("x-test").equals("value"));
    assertTrue(microserviceInstance.getProperties().get("x-test2").equals("value2"));
    assertTrue(microserviceInstance.getStatus() == MicroserviceInstanceStatus.UP);
    return true;
  }

  private void assertTrue(boolean t) {
    if (!t) {
      throw new RuntimeException();
    }
  }
}
