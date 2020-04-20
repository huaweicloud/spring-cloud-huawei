/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
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

import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;

/**
 * @Author wangqijun
 * @Date 10:49 2019-07-08
 **/

public class ServiceCombAutoServiceRegistration extends AbstractAutoServiceRegistration<ServiceCombRegistration> {
  private static final Logger LOGGER = LoggerFactory.getLogger(ServiceCombAutoServiceRegistration.class);

  private ServiceCombRegistration serviceCombRegistration;

  private ServiceCombDiscoveryProperties serviceCombDiscoveryProperties;

  public ServiceCombAutoServiceRegistration(ServiceCombServiceRegistry registry,
      AutoServiceRegistrationProperties autoServiceRegistrationProperties,
      ServiceCombRegistration registration,
      ServiceCombDiscoveryProperties serviceCombDiscoveryProperties) {
    super(registry, autoServiceRegistrationProperties);
    this.serviceCombRegistration = registration;
    this.serviceCombDiscoveryProperties = serviceCombDiscoveryProperties;
  }

  @Override
  public void start() {
    super.start();
  }

  @Override
  protected int getConfiguredPort() {
    return Integer.parseInt(serviceCombDiscoveryProperties.getPort());
  }

  @Override
  protected void setConfiguredPort(int i) {
    //never use
  }

  @Override
  protected void register() {
    super.register();
  }

  @Override
  protected Object getConfiguration() {
    return this.serviceCombRegistration.getServiceCombDiscoveryProperties();
  }

  @Override
  protected boolean isEnabled() {
    return this.serviceCombRegistration.getServiceCombDiscoveryProperties().isEnabled();
  }

  @Override
  protected ServiceCombRegistration getRegistration() {
    return this.serviceCombRegistration;
  }

  @Override
  protected ServiceCombRegistration getManagementRegistration() {
    LOGGER.info("TODO//");
    //TODO
    return null;
  }
}
