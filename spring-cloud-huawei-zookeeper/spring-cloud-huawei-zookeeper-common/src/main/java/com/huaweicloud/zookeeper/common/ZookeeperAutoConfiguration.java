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

package com.huaweicloud.zookeeper.common;

import org.apache.curator.RetryPolicy;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
@ConditionalOnZookeeperEnabled
@EnableConfigurationProperties
public class ZookeeperAutoConfiguration {
	@Bean
	@ConditionalOnMissingBean
	@ConfigurationProperties(ZookeeperProperties.PREFIX)
	public ZookeeperProperties zookeeperProperties() {
		return new ZookeeperProperties();
	}

	@Bean
	@ConditionalOnMissingBean
	public RetryPolicy exponentialBackoffRetry(ZookeeperProperties properties) {
		return CuratorFactory.retryPolicy(properties);
	}

	@Bean(destroyMethod = "close")
	@ConditionalOnMissingBean
	public CuratorFramework curatorFramework(ZookeeperProperties properties, RetryPolicy retryPolicy,
			ObjectProvider<CuratorFrameworkCustomizer> optionalCuratorFrameworkCustomizerProvider,
			ObjectProvider<EnsembleProvider> optionalEnsembleProvider,
			ObjectProvider<TracerDriver> optionalTracerDriverProvider) throws Exception {
		return CuratorFactory.curatorFramework(properties, retryPolicy,
				optionalCuratorFrameworkCustomizerProvider::orderedStream, optionalEnsembleProvider::getIfAvailable,
				optionalTracerDriverProvider::getIfAvailable);
	}
}
