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

package com.huaweicloud.zookeeper.discovery;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.curator.RetryPolicy;
import org.apache.curator.ensemble.EnsembleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CuratorUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(CuratorUtils.class);

  public static CuratorFramework createCuratorFramework(ZookeeperDiscoveryProperties properties,
      Supplier<Stream<ServiceCuratorFrameworkCustomizer>> optionalCuratorFrameworkCustomizerProvider,
      Supplier<EnsembleProvider> optionalEnsembleProvider) {
    CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
    EnsembleProvider ensembleProvider = optionalEnsembleProvider.get();
    if (ensembleProvider != null) {
      builder.ensembleProvider(ensembleProvider);
    } else {
      builder.connectString(properties.getConnectString());
    }

    // session config setting
    builder.sessionTimeoutMs((int) properties.getSessionTimeout().toMillis())
        .connectionTimeoutMs((int) properties.getConnectionTimeout().toMillis())
        .retryPolicy(retryPolicy(properties));

    // integrations extend customizers info
    Stream<ServiceCuratorFrameworkCustomizer> customizers = optionalCuratorFrameworkCustomizerProvider.get();
    if (customizers != null) {
      customizers.forEach(curatorFrameworkCustomizer -> curatorFrameworkCustomizer.customize(builder));
    }
    CuratorFramework curator = builder.build();
    curator.start();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("blocking until connected to zookeeper for " + properties.getBlockUntilConnectedWait()
          + properties.getBlockUntilConnectedUnit());
    }
    try {
      curator.blockUntilConnected(properties.getBlockUntilConnectedWait(), properties.getBlockUntilConnectedUnit());
    } catch (InterruptedException e) {
      LOGGER.error("zookeeper curator blockUntilConnected config set failed!", e);
    }
    LOGGER.info("connected to zookeeper");
    return curator;
  }

  private static RetryPolicy retryPolicy(ZookeeperDiscoveryProperties properties) {
    return new ExponentialBackoffRetry(properties.getBaseSleepTimeMs(), properties.getMaxRetries(),
        properties.getMaxSleepMs());
  }
}
