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

import com.huaweicloud.governance.properties.SerializeCache;
import com.huaweicloud.governance.service.MatchersService;
import com.huaweicloud.governance.service.MatchersServiceImpl;
import com.huaweicloud.governance.service.PolicyService;
import com.huaweicloud.governance.service.PolicyServiceImpl;
import com.huaweicloud.governance.handler.CircuitBreakerHandler;
import com.huaweicloud.governance.handler.GovManager;
import com.huaweicloud.governance.handler.RateLimitingHandler;
import com.huaweicloud.governance.properties.MatchProperties;
import com.huaweicloud.governance.marker.RequestProcessor;
import com.huaweicloud.governance.marker.operator.CompareOperator;
import com.huaweicloud.governance.marker.operator.ContainsOperator;
import com.huaweicloud.governance.marker.operator.ExactOperator;
import com.huaweicloud.governance.marker.operator.MatchOperator;
import com.huaweicloud.governance.marker.operator.RegexOperator;
import com.huaweicloud.governance.properties.RateLimitProperties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @Author GuoYl123
 * @Date 2020/5/11
 **/
@Configuration
public class GovConfiguration {

  @Bean
  public InvokeProxyAop apiModelReaderAop() {
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
}
