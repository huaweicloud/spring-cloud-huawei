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

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.client.serviceregistry.ServiceRegistry;
import org.springframework.core.env.Environment;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingMaintainService;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.google.common.eventbus.EventBus;
import com.huaweicloud.common.event.EventManager;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.NamingServiceManager;

public class NacosServiceRegistry implements ServiceRegistry<Registration> {

	private static final String STATUS_UP = "UP";

	private static final String STATUS_DOWN = "DOWN";

	private static final Logger LOGGER = LoggerFactory.getLogger(NacosServiceRegistry.class);

	private final NacosDiscoveryProperties nacosDiscoveryProperties;

	private final NamingServiceManager namingServiceManager;

	private final Instance instance;

	private final EventBus eventBus;

	public NacosServiceRegistry(NamingServiceManager namingServiceManager,
			NacosDiscoveryProperties nacosDiscoveryProperties, Environment environment) {
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
		this.namingServiceManager = namingServiceManager;
		this.instance  =
				NacosMicroserviceHandler.createMicroserviceInstance(nacosDiscoveryProperties, environment);
		eventBus = EventManager.getEventBus();
	}

	@Override
	public void register(Registration registration) {
		if (StringUtils.isEmpty(registration.getServiceId())) {
			LOGGER.warn("Have no service name to register for nacos.");
			return;
		}
		NamingService namingService = namingService();
		String serviceId = registration.getServiceId();
		String group = nacosDiscoveryProperties.getGroup();
		instance.setInstanceId(registration.getInstanceId());
		try {
			namingService.registerInstance(serviceId, group, instance);
			eventBus.post(new NacosServiceRegistrationEvent(instance, true));
			LOGGER.info("nacos registry, {} {}:{} register finished", serviceId, instance.getIp(), instance.getPort());
		} catch (Exception e) {
			eventBus.post(new NacosServiceRegistrationEvent(instance, false));
			LOGGER.error("service {} nacos registry failed", serviceId, e);
		}
	}

	@Override
	public void deregister(Registration registration) {
		LOGGER.warn("De-registery service {} from Nacos Server started.", registration.getServiceId());
		if (StringUtils.isEmpty(registration.getServiceId())) {
			LOGGER.warn("No service to de-register for nacos.");
			return;
		}
		NamingService namingService = namingService();
		String serviceId = registration.getServiceId();
		String group = nacosDiscoveryProperties.getGroup();
		try {
			namingService.deregisterInstance(serviceId, group, registration.getHost(),
					registration.getPort(), nacosDiscoveryProperties.getClusterName());
			eventBus.post(new NacosServiceRegistrationEvent(instance, false));
		} catch (Exception e) {
			LOGGER.error("de-register service {} from Nacos Server failed.", registration.getServiceId(), e);
		}

		LOGGER.info("De-registery service {} from Nacos Server finished.", registration.getServiceId());
	}

	@Override
	public void close() {
		try {
			namingServiceManager.nacosServiceShutDown();
		} catch (NacosException e) {
			LOGGER.error("Nacos namingService shutDown failed.", e);
		}
	}

	@Override
	public void setStatus(Registration registration, String status) {
		if (!STATUS_UP.equalsIgnoreCase(status) && !STATUS_DOWN.equalsIgnoreCase(status)) {
			LOGGER.warn("can't support status {} to update.", status);
			return;
		}
		String serviceId = registration.getServiceId();
    instance.setEnabled(!STATUS_DOWN.equalsIgnoreCase(status));
		try {
			namingMaintainService().updateInstance(serviceId, nacosDiscoveryProperties.getGroup(), instance);
		} catch (Exception e) {
			throw new RuntimeException("update nacos instance status fail", e);
		}
	}

	@Override
	@SuppressWarnings({"unchecked"})
	public <T> T getStatus(Registration registration) {
		String serviceName = registration.getServiceId();
		String group = nacosDiscoveryProperties.getGroup();
		try {
			List<Instance> instances = namingService().getAllInstances(serviceName, group);
			for (Instance instance : instances) {
				if (instance.getIp().equalsIgnoreCase(nacosDiscoveryProperties.getIp())
						&& instance.getPort() == nacosDiscoveryProperties.getPort()) {
					return (T) (instance.isEnabled() ? STATUS_UP : STATUS_DOWN);
				}
			}
		} catch (Exception e) {
			LOGGER.error("getStatus failed", e);
		}
		return null;
	}

	private NamingService namingService() {
		return namingServiceManager.buildNamingService();
	}

	private NamingMaintainService namingMaintainService() {
		return namingServiceManager.buildNamingMaintainService();
	}

}
