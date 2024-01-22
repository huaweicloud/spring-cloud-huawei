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

package com.huaweicloud.servicecomb.dashboard;

import java.util.List;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombSSLProperties;
import com.huaweicloud.service.engine.common.configration.dynamic.DashboardProperties;
import com.huaweicloud.servicecomb.dashboard.model.MonitorDataProvider;
import com.huaweicloud.servicecomb.dashboard.model.MonitorDataPublisher;
import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;

@Configuration
@ConditionalOnProperty(value = "spring.cloud.servicecomb.dashboard.enabled",
    havingValue = "true")
public class DashboardConfiguration {
  @Bean
  public DataFactory dataFactory(List<MonitorDataProvider> dataProviders, MonitorDataPublisher monitorDataPublisher,
      DashboardProperties dashboardProperties) {
    return new DataFactory(dataProviders, monitorDataPublisher, dashboardProperties);
  }

  @Bean
  public MonitorDataPublisher monitorDataPublisher(ServiceCombSSLProperties serviceCombSSLProperties,
      DashboardProperties dashboardProperties, List<AuthHeaderProvider> authHeaderProviders) {
    return new DefaultMonitorDataPublisher(serviceCombSSLProperties, dashboardProperties, authHeaderProviders);
  }

  @Bean
  public MonitorDataProvider governanceMonitorDataProvider(MeterRegistry meterRegistry,
      ServiceCombRegistration registration,
      DashboardProperties dashboardProperties) {
    return new GovernanceMonitorDataProvider(meterRegistry, registration, dashboardProperties);
  }

  @Bean
  public MonitorDataProvider invocationMonitorDataProvider(MeterRegistry meterRegistry,
      ServiceCombRegistration registration,
      DashboardProperties dashboardProperties) {
    return new InvocationMonitorDataProvider(meterRegistry, registration, dashboardProperties);
  }

  @Bean
  @ConditionalOnMissingBean
  public MeterRegistry meterRegistry() {
    return new SimpleMeterRegistry();
  }
}
