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

import com.huaweicloud.governance.client.FeignProxyAop;
import com.huaweicloud.governance.client.GovRibbonServerFilter;
import com.huaweicloud.governance.client.RestTemplateProxyAop;
import com.huaweicloud.common.ribbon.ServiceCombLoadBalanceRule;
import com.huaweicloud.governance.handler.BulkheadHandler;
import com.huaweicloud.governance.handler.RetryHandler;
import com.huaweicloud.governance.properties.BulkheadProperties;
import com.huaweicloud.governance.properties.CircuitBreakerProperties;
import com.huaweicloud.governance.properties.RetryProperties;
import com.huaweicloud.governance.properties.SerializeCache;
import com.huaweicloud.governance.service.MatchersService;
import com.huaweicloud.governance.service.MatchersServiceImpl;
import com.huaweicloud.governance.service.PolicyService;
import com.huaweicloud.governance.service.PolicyServiceImpl;
import com.huaweicloud.governance.handler.CircuitBreakerHandler;
import com.huaweicloud.governance.handler.RateLimitingHandler;
import com.huaweicloud.governance.properties.MatchProperties;
import com.huaweicloud.governance.marker.RequestProcessor;
import com.huaweicloud.governance.marker.operator.CompareOperator;
import com.huaweicloud.governance.marker.operator.ContainsOperator;
import com.huaweicloud.governance.marker.operator.ExactOperator;
import com.huaweicloud.governance.marker.operator.MatchOperator;
import com.huaweicloud.governance.marker.operator.RegexOperator;
import com.huaweicloud.governance.properties.RateLimitProperties;
import com.netflix.client.config.IClientConfig;
import com.netflix.loadbalancer.IRule;
import com.netflix.loadbalancer.ZoneAvoidanceRule;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
@Configuration
public class GovConfiguration {

  @Bean
  public InvokeProxyAop invokeProxyAop() {
    return new InvokeProxyAop();
  }

  @Bean
  public MatchProperties matchProperties() {
    return new MatchProperties();
  }

  @Bean
  public RateLimitProperties rateLimitProperties() {
    return new RateLimitProperties();
  }

  @Bean
  public CircuitBreakerProperties circuitBreakerProperties() {
    return new CircuitBreakerProperties();
  }

  @Bean
  public RetryProperties retryProperties() {
    return new RetryProperties();
  }

  @Bean
  public BulkheadProperties bulkheadProperties() {
    return new BulkheadProperties();
  }

  @Bean
  public MatchersManager matchersManager() {
    return new MatchersManager();
  }

  @Bean
  public SerializeCache serializeCache() {
    return new SerializeCache();
  }

  @Bean
  public MatchersService matchersService() {
    return new MatchersServiceImpl();
  }

  @Bean
  public PolicyService policyService() {
    return new PolicyServiceImpl();
  }

  @Bean
  public GovManager processorManager() {
    return new GovManager();
  }

  @Bean(name = "GovRateLimiting")
  public RateLimitingHandler rateLimitingHandler() {
    return new RateLimitingHandler();
  }

  @Bean(name = "GovCircuitBreaker")
  public CircuitBreakerHandler circuitBreakerHandler() {
    return new CircuitBreakerHandler();
  }

  @Bean(name = "GovRetry")
  public RetryHandler retryHandler() {
    return new RetryHandler();
  }

  @Bean(name = "GovBulkhead")
  public BulkheadHandler bulkheadHandler() {
    return new BulkheadHandler();
  }

  @Bean(name = "exactOperator")
  public MatchOperator exactOperator() {
    return new ExactOperator();
  }

  @Bean(name = "regexOperator")
  public MatchOperator regexOperator() {
    return new RegexOperator();
  }

  @Bean(name = "containsOperator")
  public MatchOperator containsOperator() {
    return new ContainsOperator();
  }

  @Bean(name = "compareOperator")
  public MatchOperator compareOperator() {
    return new CompareOperator();
  }

  @Bean
  public RequestProcessor operatorProcessor() {
    return new RequestProcessor();
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
