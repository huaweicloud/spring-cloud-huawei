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

import java.net.URI;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.cloud.client.DefaultServiceInstance;
import org.springframework.cloud.client.serviceregistry.Registration;

import com.alibaba.nacos.api.naming.PreservedMetadataKeys;
import com.huaweicloud.nacos.discovery.NacosDiscoveryProperties;
import com.huaweicloud.nacos.discovery.utils.NetUtils;

public class NacosRegistration implements Registration {
	private static final String IPV6 = "IPv6";

	private final List<NacosRegistrationMetadataCustomizer> registrationCustomizers;

	private final NacosDiscoveryProperties nacosDiscoveryProperties;

	private String instanceId;

	public NacosRegistration(List<NacosRegistrationMetadataCustomizer> registrationCustomizers,
			NacosDiscoveryProperties nacosDiscoveryProperties) {
		this.registrationCustomizers = registrationCustomizers;
		this.nacosDiscoveryProperties = nacosDiscoveryProperties;
		init();
	}

	public void init() {
		buildPropertiesAttributes();
		this.instanceId = buildInstanceId();
	}

	private void buildPropertiesAttributes() {
		Map<String, String> metadata = nacosDiscoveryProperties.getMetadata();
		metadata.put(PreservedMetadataKeys.REGISTER_SOURCE, "SPRING_CLOUD");
		if (nacosDiscoveryProperties.isSecure()) {
			metadata.put("secure", "true");
		}
		if (null != nacosDiscoveryProperties.getHeartBeatInterval()) {
			metadata.put(PreservedMetadataKeys.HEART_BEAT_INTERVAL,
					nacosDiscoveryProperties.getHeartBeatInterval().toString());
		}
		if (null != nacosDiscoveryProperties.getHeartBeatTimeout()) {
			metadata.put(PreservedMetadataKeys.HEART_BEAT_TIMEOUT,
					nacosDiscoveryProperties.getHeartBeatTimeout().toString());
		}
		if (null != nacosDiscoveryProperties.getIpDeleteTimeout()) {
			metadata.put(PreservedMetadataKeys.IP_DELETE_TIMEOUT,
					nacosDiscoveryProperties.getIpDeleteTimeout().toString());
		}
		customize(registrationCustomizers);
		if (StringUtils.isEmpty(nacosDiscoveryProperties.getIp())) {
			String ip = IPV6.equalsIgnoreCase(nacosDiscoveryProperties.getIpType())
					? NetUtils.getIpv6HostAddress() : NetUtils.getHostAddress();
			nacosDiscoveryProperties.setIp(ip);
		}
	}

	protected void customize(
			List<NacosRegistrationMetadataCustomizer> registrationCustomizers) {
		if (registrationCustomizers != null) {
			for (NacosRegistrationMetadataCustomizer customizer : registrationCustomizers) {
				customizer.customize(this);
			}
		}
	}

	@Override
	public String getInstanceId() {
		return instanceId;
	}

	@Override
	public String getServiceId() {
		return nacosDiscoveryProperties.getService();
	}

	@Override
	public String getHost() {
		return nacosDiscoveryProperties.getIp();
	}

	@Override
	public int getPort() {
		return nacosDiscoveryProperties.getPort();
	}

	@Override
	public boolean isSecure() {
		return nacosDiscoveryProperties.isSecure();
	}

	@Override
	public URI getUri() {
		return DefaultServiceInstance.getUri(this);
	}

	@Override
	public Map<String, String> getMetadata() {
		return nacosDiscoveryProperties.getMetadata();
	}

	public NacosDiscoveryProperties getNacosDiscoveryProperties() {
		return nacosDiscoveryProperties;
	}

	private String buildInstanceId() {
		String result = nacosDiscoveryProperties.getIp() + ":" + nacosDiscoveryProperties.getPort();
		return result.replaceAll("[^0-9a-zA-Z]", "-");
	}
}
