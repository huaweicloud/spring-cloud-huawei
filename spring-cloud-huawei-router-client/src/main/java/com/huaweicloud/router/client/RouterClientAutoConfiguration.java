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
package com.huaweicloud.router.client;

import org.apache.servicecomb.router.distribute.AbstractRouterDistributor;
import org.apache.servicecomb.service.center.client.model.MicroserviceInstance;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.ribbon.RibbonAutoConfiguration;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.router.client.ribbon.RouterRibbonClientsConfiguration;
import com.netflix.loadbalancer.Server;

@Configuration
@EnableConfigurationProperties
@ComponentScan(basePackages = {"org.apache.servicecomb.router"})

@AutoConfigureAfter(RibbonAutoConfiguration.class)
@RibbonClients(defaultConfiguration = RouterRibbonClientsConfiguration.class)
@EnableFeignClients
public class RouterClientAutoConfiguration {
  @Bean
  public AbstractRouterDistributor<Server, MicroserviceInstance> routerDistributor() {
    return new SpringCloudRouterDistributor();
  }
}