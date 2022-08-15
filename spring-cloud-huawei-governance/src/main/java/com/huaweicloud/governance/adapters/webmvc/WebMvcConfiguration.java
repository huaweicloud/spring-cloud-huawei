/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.governance.adapters.webmvc;

import org.apache.servicecomb.governance.handler.BulkheadHandler;
import org.apache.servicecomb.governance.handler.CircuitBreakerHandler;
import org.apache.servicecomb.governance.handler.IdentifierRateLimitingHandler;
import org.apache.servicecomb.governance.handler.RateLimitingHandler;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;

@Configuration
@ConditionalOnClass(name = "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter")
public class WebMvcConfiguration {
  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_GOVERNANCE_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public GovernanceRequestMappingHandlerAdapter governanceRequestMappingHandlerAdapter(
      CircuitBreakerHandler circuitBreakerHandler,
      BulkheadHandler bulkheadHandler) {
    return new GovernanceRequestMappingHandlerAdapter(circuitBreakerHandler, bulkheadHandler);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_RATE_LIMITING_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public FilterRegistrationBean<RateLimitingFilter> rateLimitingFilter(
      RateLimitingHandler rateLimitingHandler,
      GovernanceProperties governanceProperties) {
    FilterRegistrationBean<RateLimitingFilter> registrationBean
        = new FilterRegistrationBean<>();

    registrationBean.setFilter(new RateLimitingFilter(rateLimitingHandler));
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(governanceProperties.getWebmvc().getRateLimiting().getOrder());

    return registrationBean;
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_IDENTIFIER_RATE_LIMITING_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public FilterRegistrationBean<IdentifierRateLimitingFilter> identifierRateLimitingFilter(
      IdentifierRateLimitingHandler identifierRateLimitingHandler,
      GovernanceProperties governanceProperties) {
    FilterRegistrationBean<IdentifierRateLimitingFilter> registrationBean
        = new FilterRegistrationBean<>();

    registrationBean.setFilter(new IdentifierRateLimitingFilter(identifierRateLimitingHandler));
    registrationBean.addUrlPatterns("/*");
    registrationBean.setOrder(governanceProperties.getWebmvc().getIdentifierRateLimiting().getOrder());

    return registrationBean;
  }
}
