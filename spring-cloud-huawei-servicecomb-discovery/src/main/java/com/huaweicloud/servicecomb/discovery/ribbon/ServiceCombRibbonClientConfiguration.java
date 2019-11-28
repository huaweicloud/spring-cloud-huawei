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

package com.huaweicloud.servicecomb.discovery.ribbon;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import com.huaweicloud.servicecomb.discovery.discovery.ServiceCombDiscoveryProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.ServerList;

/**
 * @Author wangqijun
 * @Date 17:15 2019-07-11
 **/
@Configuration
public class ServiceCombRibbonClientConfiguration {


  @Bean
  @ConditionalOnMissingBean
  public ServerList<?> ribbonServerList(IClientConfig config,
      ServiceCombDiscoveryProperties serviceCombProperties) {
    ServiceCombServerList serverList = new ServiceCombServerList(serviceCombProperties);
    serverList.initWithNiwsConfig(config);
    return serverList;
  }
}
