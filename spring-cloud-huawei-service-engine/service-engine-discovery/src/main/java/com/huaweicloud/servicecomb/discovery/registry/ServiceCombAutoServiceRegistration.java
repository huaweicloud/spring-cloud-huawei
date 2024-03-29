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

package com.huaweicloud.servicecomb.discovery.registry;

import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;

public class ServiceCombAutoServiceRegistration extends AbstractAutoServiceRegistration<ServiceCombRegistration> {
  private final ServiceCombRegistration serviceCombRegistration;

  public ServiceCombAutoServiceRegistration(ServiceCombServiceRegistry registry,
      AutoServiceRegistrationProperties autoServiceRegistrationProperties, ServiceCombRegistration registration) {
    super(registry, autoServiceRegistrationProperties);
    this.serviceCombRegistration = registration;
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  protected void register() {
    super.register();
  }

  @Override
  @Deprecated
  protected Object getConfiguration() {
    return this.serviceCombRegistration.getDiscoveryBootstrapProperties();
  }

  @Override
  protected boolean isEnabled() {
    return this.serviceCombRegistration.getDiscoveryBootstrapProperties().isEnabled();
  }

  @Override
  protected ServiceCombRegistration getRegistration() {
    return this.serviceCombRegistration;
  }

  @Override
  protected ServiceCombRegistration getManagementRegistration() {
    return null;
  }
}
