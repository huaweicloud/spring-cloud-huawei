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
package com.huaweicloud.governance;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.ribbon.ServiceCombLoadBalanceRule;
import com.huaweicloud.governance.client.FeignProxyAop;
import com.huaweicloud.governance.client.GovRibbonServerFilter;
import com.huaweicloud.governance.client.RestTemplateProxyAop;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
@Configuration
@ComponentScan(basePackages = {"com.huaweicloud.governance"})
public class GovConfiguration {

  @Bean
  public InvokeProxyAop invokeProxyAop() {
    return new InvokeProxyAop();
  }


  @Bean
  public GovManager processorManager() {
    return new GovManager();
  }


  @Bean
  public IRule ribbonRule(@Autowired(required = false) IClientConfig config) {
    ZoneAvoidanceRule rule = new ServiceCombLoadBalanceRule();
    rule.initWithNiwsConfig(config);
    return rule;
  }

  @Bean
  public GovRibbonServerFilter govRibbonServerFilter() {
    return new GovRibbonServerFilter();
  }

  @Bean
  public RestTemplateProxyAop restTemplateProxyAop() {
    return new RestTemplateProxyAop();
  }

  @Bean
  @ConditionalOnBean(RibbonLoadBalancerClient.class)
  public FeignProxyAop feignProxyAop() {
    return new FeignProxyAop();
  }
}
