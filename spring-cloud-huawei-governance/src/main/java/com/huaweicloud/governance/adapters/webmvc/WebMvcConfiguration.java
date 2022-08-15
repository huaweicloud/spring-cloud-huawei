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
import org.apache.servicecomb.service.center.client.ServiceCenterClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.governance.authentication.provider.BlackWhiteListProperties;
import com.huaweicloud.governance.authentication.provider.ProviderAuthPreHandlerInterceptor;

@Configuration
@ConditionalOnClass(name = "org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter")
public class WebMvcConfiguration {
  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_GOVERNANCE_ENABLED,
      havingValue = "true", matchIfMissing = true)
  public GovernanceRequestMappingHandlerAdapter governanceRequestMappingHandlerAdapter(
      RateLimitingHandler rateLimitingHandler, CircuitBreakerHandler circuitBreakerHandler,
      BulkheadHandler bulkheadHandler,@Autowired(required = false) ProviderAuthHandler providerAuthHandler
  ) {
    return new GovernanceRequestMappingHandlerAdapter(rateLimitingHandler, circuitBreakerHandler, bulkheadHandler,
        providerAuthHandler);
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

  @Bean
  @RefreshScope
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.webmvc.governance.publickey.provider.enabled",
      havingValue = "true")
  @ConfigurationProperties("servicecomb.publickey.accesscontrol")
  public BlackWhiteListProperties blackWhiteListProperties() {
    return new BlackWhiteListProperties();
  }

  @Bean
  @ConditionalOnProperty(value = "spring.cloud.servicecomb.webmvc.governance.publickey.provider.enabled",
      havingValue = "true")
  public ProviderAuthPreHandlerInterceptor providerAuthPreHandlerInterceptor(ServiceCenterClient client,
      BlackWhiteListProperties blackWhiteListProperties) {
    return new ProviderAuthPreHandlerInterceptor(client, blackWhiteListProperties);
  }
}
