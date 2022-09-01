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

package com.huaweicloud.common.adapters.webflux;

import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.server.WebFilter;

import com.huaweicloud.common.access.AccessLogLogger;
import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.common.event.ClosedEventListener;
import com.huaweicloud.common.metrics.InvocationMetrics;

@Configuration
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class WebFluxConfiguration {
  @Bean
  public WebFilter invocationContextWebFilter(ContextProperties contextProperties) {
    return new InvocationContextWebFilter(contextProperties);
  }

  @Bean
  public WebFilter accessLogWebFilter(ContextProperties contextProperties, AccessLogLogger accessLogLogger) {
    return new AccessLogWebFilter(contextProperties, accessLogLogger);
  }

  @Bean
  public WebFilter invocationMetricsWebFilter(InvocationMetrics invocationMetrics,
      GovernanceProperties governanceProperties) {
    return new InvocationMetricsWebFilter(invocationMetrics, governanceProperties);
  }

  @Bean
  public WebFilter shutdownHookWebFilter(ContextProperties contextProperties,
      ClosedEventListener closedEventListener) {
    return new ShutdownHookWebFilter(contextProperties, closedEventListener);
  }
}
