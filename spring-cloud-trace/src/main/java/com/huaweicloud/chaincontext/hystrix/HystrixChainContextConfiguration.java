/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.huaweicloud.chaincontext.hystrix;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Configuration;

import com.netflix.hystrix.strategy.HystrixPlugins;
import com.netflix.hystrix.strategy.concurrency.HystrixConcurrencyStrategy;
import com.netflix.hystrix.strategy.eventnotifier.HystrixEventNotifier;
import com.netflix.hystrix.strategy.executionhook.HystrixCommandExecutionHook;
import com.netflix.hystrix.strategy.metrics.HystrixMetricsPublisher;
import com.netflix.hystrix.strategy.properties.HystrixPropertiesStrategy;
import com.netflix.ribbon.proxy.annotation.Hystrix;


@Configuration
@ConditionalOnClass({Hystrix.class})
public class HystrixChainContextConfiguration {

  @Autowired(required = false)
  private HystrixConcurrencyStrategy existingConcurrencyStrategy;

  @PostConstruct
  public void init() {
    // Keeps references of existing Hystrix plugins.
    HystrixEventNotifier eventNotifier = HystrixPlugins.getInstance().getEventNotifier();
    HystrixMetricsPublisher metricsPublisher = HystrixPlugins.getInstance().getMetricsPublisher();
    HystrixPropertiesStrategy propertiesStrategy = HystrixPlugins.getInstance().getPropertiesStrategy();
    HystrixCommandExecutionHook commandExecutionHook = HystrixPlugins.getInstance().getCommandExecutionHook();

    HystrixPlugins.reset();

    // Registers existing plugins excepts the Concurrent Strategy plugin.
    HystrixPlugins.getInstance()
        .registerConcurrencyStrategy(new ChainContextConcurrencyStrategy(existingConcurrencyStrategy));
    HystrixPlugins.getInstance().registerEventNotifier(eventNotifier);
    HystrixPlugins.getInstance().registerMetricsPublisher(metricsPublisher);
    HystrixPlugins.getInstance().registerPropertiesStrategy(propertiesStrategy);
    HystrixPlugins.getInstance().registerCommandExecutionHook(commandExecutionHook);
  }
}
