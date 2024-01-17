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

package com.huaweicloud.governance.adapters.loadbalancer;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.DefaultResponse;
import org.springframework.cloud.client.loadbalancer.EmptyResponse;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.loadbalancer.core.ReactorServiceInstanceLoadBalancer;
import org.springframework.cloud.loadbalancer.core.SelectedInstanceCallback;
import org.springframework.cloud.loadbalancer.core.ServiceInstanceListSupplier;
import org.springframework.cloud.loadbalancer.core.WeightedServiceInstanceListSupplier;

import com.huaweicloud.governance.adapters.GovernanceHeaderStatusUtils;

import reactor.core.publisher.Mono;

/**
 * Wieghted loadbancer
 */
public class InstanceWeightedLoadBalancer implements ReactorServiceInstanceLoadBalancer {
	private static final Logger LOGGER = LoggerFactory.getLogger(InstanceWeightedLoadBalancer.class);

	final AtomicInteger position;

	final String serviceId;

	final WeightedServiceInstanceListSupplier supplier;

	public InstanceWeightedLoadBalancer(WeightedServiceInstanceListSupplier serviceInstanceListSupplierProvider,
			String serviceId) {
		this.serviceId = serviceId;
		this.supplier = serviceInstanceListSupplierProvider;
		this.position = new AtomicInteger(GovernanceHeaderStatusUtils.random.nextInt(1000));
	}

	@SuppressWarnings("rawtypes")
	@Override
	public Mono<Response<ServiceInstance>> choose(Request request) {
		return supplier.get(request).next()
				.map(serviceInstances -> processInstanceResponse(supplier, serviceInstances));
	}

	private Response<ServiceInstance> processInstanceResponse(ServiceInstanceListSupplier supplier,
			List<ServiceInstance> serviceInstances) {
		Response<ServiceInstance> serviceInstanceResponse = getInstanceResponse(serviceInstances);
		if (supplier instanceof SelectedInstanceCallback && serviceInstanceResponse.hasServer()) {
			((SelectedInstanceCallback) supplier).selectedServiceInstance(serviceInstanceResponse.getServer());
		}
		return serviceInstanceResponse;
	}

	/**
	 * instances为权重计算完之后所有实例数
	 * 比如2个实例instance1、instance2，,权重分别为1、2，那么实例总数则为3，相对于服务发现时的实例，instance2增加一个实例作为负载实例选择
	 * @param instances 实例总数
	 * @return 选择的实例
	 */
	private Response<ServiceInstance> getInstanceResponse(List<ServiceInstance> instances) {
		if (instances.isEmpty()) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No servers available for service: " + serviceId);
			}
			return new EmptyResponse();
		}

		if (instances.size() == 1) {
			return new DefaultResponse(instances.get(0));
		}
		int pos = this.position.incrementAndGet() & Integer.MAX_VALUE;

		ServiceInstance instance = instances.get(pos % instances.size());

		return new DefaultResponse(instance);
	}

}
