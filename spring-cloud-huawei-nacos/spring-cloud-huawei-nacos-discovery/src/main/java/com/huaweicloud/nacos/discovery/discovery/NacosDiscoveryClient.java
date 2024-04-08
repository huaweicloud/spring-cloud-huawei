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

package com.huaweicloud.nacos.discovery.discovery;

import java.util.List;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;

public class NacosDiscoveryClient implements DiscoveryClient {
	public static final String DESCRIPTION = "Nacos Discovery Client";

	private final NacosDiscovery serviceDiscovery;

	public NacosDiscoveryClient(NacosDiscovery nacosDiscovery) {
		this.serviceDiscovery = nacosDiscovery;
	}

	@Override
	public String description() {
		return DESCRIPTION;
	}

	@Override
	public List<ServiceInstance> getInstances(String serviceId) {
		try {
			return serviceDiscovery.getInstances(serviceId);
		} catch (Exception e) {
			throw new RuntimeException("Can not get hosts from nacos server. serviceId: " + serviceId, e);
		}
	}

	@Override
	public List<String> getServices() {
		try {
			return serviceDiscovery.getServices();
		} catch (Exception e) {
			throw new RuntimeException("get service name from nacos server failed.", e);
		}
	}
}
