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

import java.io.Serial;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.curator.RetryPolicy;
import org.apache.curator.drivers.TracerDriver;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.BootstrapContext;
import org.springframework.boot.BootstrapRegistry;
import org.springframework.boot.context.config.ConfigDataException;
import org.springframework.boot.context.properties.bind.Bindable;
import org.springframework.boot.context.properties.bind.Binder;

public class CuratorFactory {
	private static final Logger LOGGER = LoggerFactory.getLogger(CuratorFactory.class);

	public static CuratorFramework curatorFramework(ZookeeperProperties properties, RetryPolicy retryPolicy,
			Supplier<Stream<CuratorFrameworkCustomizer>> optionalCuratorFrameworkCustomizerProvider,
			Supplier<EnsembleProvider> optionalEnsembleProvider, Supplier<TracerDriver> optionalTracerDriverProvider)
			throws Exception {
		CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
		EnsembleProvider ensembleProvider = optionalEnsembleProvider.get();
		if (ensembleProvider != null) {
			builder.ensembleProvider(ensembleProvider);
		} else {
			builder.connectString(properties.getConnectString());
		}

		// session config setting
		builder.sessionTimeoutMs((int) properties.getSessionTimeout().toMillis())
				.connectionTimeoutMs((int) properties.getConnectionTimeout().toMillis()).retryPolicy(retryPolicy);

		// integrations extend customizers info
		Stream<CuratorFrameworkCustomizer> customizers = optionalCuratorFrameworkCustomizerProvider.get();
		if (customizers != null) {
			customizers.forEach(curatorFrameworkCustomizer -> curatorFrameworkCustomizer.customize(builder));
		}

		CuratorFramework curator = builder.build();
		TracerDriver tracerDriver = optionalTracerDriverProvider.get();
		if (tracerDriver != null && curator.getZookeeperClient() != null) {
			curator.getZookeeperClient().setTracerDriver(tracerDriver);
		}

		curator.start();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("blocking until connected to zookeeper for " + properties.getBlockUntilConnectedWait()
					+ properties.getBlockUntilConnectedUnit());
		}
		curator.blockUntilConnected(properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
		LOGGER.info("connected to zookeeper");
		return curator;
	}

	public static RetryPolicy retryPolicy(ZookeeperProperties properties) {
		return new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(), properties.getMaxRetries(),
				properties.getMaxSleepMs());
	}

	public static void registerCurator(BootstrapRegistry registery) {
		registerCurator(registery, bootstrapContext -> true);
	}

	public static void registerCurator(BootstrapRegistry registery, Predicate<BootstrapContext> predicate) {
		registery.registerIfAbsent(ZookeeperProperties.class, context ->
				predicate.test(context) ? loadProperties(context.get(Binder.class)) : null
		);

		registery.registerIfAbsent(RetryPolicy.class, context ->
				predicate.test(context) ? retryPolicy(context.get(ZookeeperProperties.class)) : null
		);

		registery.registerIfAbsent(CuratorFramework.class, context ->
				predicate.test(context) ? curatorFramework(context, context.get(ZookeeperProperties.class)) : null
		);

		// promote beans to context
		registery.addCloseListener(event -> {
			BootstrapContext context = event.getBootstrapContext();
			if (predicate.test(context)) {
				CuratorFramework curatorFramework = context.get(CuratorFramework.class);
				if (!event.getApplicationContext().getBeanFactory().containsBean("configDataCuratorFramework")) {
					event.getApplicationContext().getBeanFactory()
							.registerSingleton("configDataCuratorFramework", curatorFramework);
				}
			}
		});
	}

	private static ZookeeperProperties loadProperties(Binder binder) {
		return binder.bind(ZookeeperProperties.PREFIX, Bindable.of(ZookeeperProperties.class))
				.orElse(new ZookeeperProperties());
	}

	private static CuratorFramework curatorFramework(BootstrapContext context, ZookeeperProperties properties) {
		Supplier<Stream<CuratorFrameworkCustomizer>> customizers;
		try {
			CuratorFrameworkCustomizer customizer = context.get(CuratorFrameworkCustomizer.class);
			customizers = () -> Stream.of(customizer);
		} catch (IllegalStateException e) {
			customizers = () -> null;
		}
		try {
			return CuratorFactory.curatorFramework(properties, context.get(RetryPolicy.class), customizers,
					supplier(context, EnsembleProvider.class), supplier(context, TracerDriver.class));
		} catch (Exception e) {
			LOGGER.error("Unable to connect to zookeeper", e);
			throw new ZookeeperConnectException("Unable to connect to zookeeper", e);
		}
	}

	private static <T> Supplier<T> supplier(BootstrapContext context, Class<T> type) {
		try {
			T instance = context.get(type);
			return () -> instance;
		}
		catch (IllegalStateException e) {
			return () -> null;
		}
	}

	private static class ZookeeperConnectException extends ConfigDataException {
		@Serial
		private static final long serialVersionUID = 1L;

		/**
		 * Create a new {@link ConfigDataException} instance.
		 * @param message the exception message
		 * @param cause the exception cause
		 */
		protected ZookeeperConnectException(String message, Throwable cause) {
			super(message, cause);
		}
	}

}
