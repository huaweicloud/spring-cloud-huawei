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

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.NacosServiceInstance;
import com.huaweicloud.nacos.discovery.manager.NamingServiceManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.core.Ordered;

public class NacosDiscovery {
	private static final Logger LOGGER = LoggerFactory.getLogger(NacosDiscovery.class);

	private final NacosDiscoveryProperties discoveryProperties;

	private final List<NamingServiceManager> namingServiceManagers;

	private final NacosCrossGroupProperties crossGroupProperties;

	public NacosDiscovery(NacosDiscoveryProperties discoveryProperties, List<NamingServiceManager> namingServiceManagers,
			NacosCrossGroupProperties crossGroupProperties) {
		this.discoveryProperties = discoveryProperties;
		this.namingServiceManagers = namingServiceManagers.stream().sorted(Comparator.comparingInt(Ordered::getOrder))
				.collect(Collectors.toList());
		this.crossGroupProperties = crossGroupProperties;
	}

	public List<ServiceInstance> getInstances(String serviceId) throws NacosException {
		String group = discoveryProperties.getGroup();
		if (crossGroupProperties.isEnabled() && crossGroupProperties.getServiceGroupMappings().containsKey(serviceId)) {
			// if cross group is enable and serviceId has set, using set GROUP to query instances.
			group = crossGroupProperties.getServiceGroupMappings().get(serviceId);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.warn("cross group query nacos instances by setting group {}", group);
			}
		}
		for (int i = 0; i < namingServiceManagers.size(); i++) {
			try {
				List<Instance> instances = namingServiceManagers.get(i).getNamingService()
						.selectInstances(serviceId, group, true);
				return hostToServiceInstanceList(instances, serviceId);
			} catch (NacosException e) {
				if (namingServiceManagers.size() == 1 || i == namingServiceManagers.size() - 1) {
					LOGGER.error("get service [{}] instances from nacos failed.", serviceId, e);
					throw e;
				}
				LOGGER.warn("get service [{}] instances from nacos server [{}] failed!", serviceId,
						namingServiceManagers.get(i).getServerAddr());
			}
		}
		return null;
	}

	public List<String> getServices() throws NacosException {
		String group = discoveryProperties.getGroup();
		for (int i = 0; i < namingServiceManagers.size(); i++) {
			try {
				ListView<String> services = namingServiceManagers.get(i).getNamingService()
						.getServicesOfServer(1, Integer.MAX_VALUE, group);
				return services.getData();
			} catch (NacosException e) {
				if (namingServiceManagers.size() == 1 || i == namingServiceManagers.size() - 1) {
					LOGGER.error("get service names from nacos failed!", e);
					throw e;
				}
				LOGGER.warn("get service names from nacos server [{}] failed!", namingServiceManagers.get(i).getServerAddr());
			}
		}
		return null;
	}

	private List<ServiceInstance> hostToServiceInstanceList(List<Instance> instances, String serviceId) {
		List<ServiceInstance> result = new ArrayList<>(instances.size());
		for (Instance instance : instances) {
			ServiceInstance serviceInstance = hostToServiceInstance(instance, serviceId);
			if (serviceInstance != null) {
				result.add(serviceInstance);
			}
		}
		return result;
	}

	public ServiceInstance hostToServiceInstance(Instance instance,
			String serviceId) {
		if (instance == null || !instance.isEnabled() || !instance.isHealthy()) {
			return null;
		}
		NacosServiceInstance nacosServiceInstance = new NacosServiceInstance();
		nacosServiceInstance.setHost(instance.getIp());
		nacosServiceInstance.setPort(instance.getPort());
		nacosServiceInstance.setServiceId(serviceId);
		nacosServiceInstance.setInstanceId(instance.getInstanceId());

		Map<String, String> metadata = new HashMap<>();
		metadata.put("nacos.weight", instance.getWeight() + "");
		metadata.put("nacos.cluster", instance.getClusterName());
		if (instance.getMetadata() != null) {
			metadata.putAll(instance.getMetadata());
		}
		metadata.put("nacos.ephemeral", String.valueOf(instance.isEphemeral()));
		nacosServiceInstance.setMetadata(metadata);

		if (metadata.containsKey("secure")) {
			boolean secure = Boolean.parseBoolean(metadata.get("secure"));
			nacosServiceInstance.setSecure(secure);
		}
		return nacosServiceInstance;
	}
}
