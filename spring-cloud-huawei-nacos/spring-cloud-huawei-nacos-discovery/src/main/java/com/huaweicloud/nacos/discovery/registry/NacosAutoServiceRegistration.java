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

package com.huaweicloud.nacos.discovery.registry;

import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.discovery.event.InstanceRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.AbstractAutoServiceRegistration;
import org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;

public class NacosAutoServiceRegistration extends AbstractAutoServiceRegistration<Registration> {

	private final NacosRegistration registration;

	private final ServiceRegistry<Registration> serviceRegistry;

	private boolean registryEnabled;

	public NacosAutoServiceRegistration(ServiceRegistry<Registration> serviceRegistry,
			AutoServiceRegistrationProperties autoServiceRegistrationProperties, NacosRegistration registration) {
		super(serviceRegistry, autoServiceRegistrationProperties);
		this.registration = registration;
		this.serviceRegistry = serviceRegistry;
		this.registryEnabled = registration.getNacosDiscoveryProperties().isRegisterEnabled();
	}

	@Override
	protected NacosRegistration getRegistration() {
		if (registration.getPort() < 0) {
			throw new RuntimeException("service port not set.");
		}
		return this.registration;
	}

	@Override
	protected NacosRegistration getManagementRegistration() {
		return null;
	}

	@Override
	protected void register() {
		if (!this.registryEnabled) {
			return;
		}
		if (this.registration.getPort() < 0) {
			throw new RuntimeException("service port not set.");
		}
		super.register();
	}

	@Override
	protected void registerManagement() {
		if (!this.registryEnabled) {
			return;
		}
		super.registerManagement();
	}

	@Override
	@Deprecated
	protected Object getConfiguration() {
		return this.registration.getNacosDiscoveryProperties();
	}

	@Override
	protected boolean isEnabled() {
		return this.registryEnabled;
	}

	public void setRegistryEnabled(boolean enabled) {
		registryEnabled = enabled;
	}

	@Override
	public void start() {
		beforeRegistryProcess();
		if (isEnabled()) {
			registryExtend();
		}
	}

	private void beforeRegistryProcess() {
		super.getContext().publishEvent(new InstancePreRegisteredEvent(this, getRegistration()));
	}

	public void registryExtend() {
		serviceRegistry.register(registration);
		afterRegistryProcess();
	}

	private void afterRegistryProcess() {
		if (shouldRegisterManagement()) {
			this.registerManagement();
		}
		super.getContext().publishEvent(new InstanceRegisteredEvent<>(this, getConfiguration()));
	}

	@Override
	public void stop() {
		if (isEnabled()) {
			deregister();
			if (shouldRegisterManagement()) {
				deregisterManagement();
			}
			this.serviceRegistry.close();
		}
	}
}
