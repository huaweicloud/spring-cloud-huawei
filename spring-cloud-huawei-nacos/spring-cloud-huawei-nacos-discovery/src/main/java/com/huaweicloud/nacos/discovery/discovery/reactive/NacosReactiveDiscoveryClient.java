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

package com.huaweicloud.nacos.discovery.discovery.reactive;

import java.util.function.Function;

import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.ReactiveDiscoveryClient;

import com.alibaba.nacos.api.exception.NacosException;
import com.huaweicloud.nacos.discovery.discovery.NacosDiscovery;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

public class NacosReactiveDiscoveryClient implements ReactiveDiscoveryClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(NacosReactiveDiscoveryClient.class);

	private final NacosDiscovery nacosDiscovery;

	public NacosReactiveDiscoveryClient(NacosDiscovery nacosDiscovery) {
		this.nacosDiscovery = nacosDiscovery;
	}

	@Override
	public String description() {
		return "Nacos Reactive Discovery Client";
	}

	@Override
	public Flux<ServiceInstance> getInstances(String serviceId) {
		return Mono.justOrEmpty(serviceId).flatMapMany(loadInstancesFromNacos())
				.subscribeOn(Schedulers.boundedElastic());
	}

	private Function<String, Publisher<ServiceInstance>> loadInstancesFromNacos() {
		return serviceId -> {
			try {
				return Flux.fromIterable(nacosDiscovery.getInstances(serviceId));
			} catch (NacosException e) {
				LOGGER.error("get service {} instance from nacos failed.", serviceId, e);
				return Flux.empty();
			}
		};
	}

	@Override
	public Flux<String> getServices() {
		return Flux.defer(() -> {
			try {
				return Flux.fromIterable(nacosDiscovery.getServices());
			} catch (Exception e) {
				LOGGER.error("get services from nacos failed.", e);
				return Flux.empty();
			}
		}).subscribeOn(Schedulers.boundedElastic());
	}

}
