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
package com.huaweicloud.governance;

import java.util.List;

import org.apache.servicecomb.governance.MicroserviceMeta;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.handler.ext.AbstractCircuitBreakerExtension;
import org.apache.servicecomb.governance.handler.ext.AbstractInstanceIsolationExtension;
import org.apache.servicecomb.governance.handler.ext.AbstractRetryExtension;
import org.apache.servicecomb.governance.properties.MapperProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cloud.context.environment.EnvironmentChangeEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"org.apache.servicecomb.governance"})
public class GovernanceConfiguration {
  @Bean
  public MapperProperties contextMapperProperties() {
    return new MapperProperties("servicecomb.contextMapper");
  }

  @Bean
  public MapperHandler contextMapperHandler(@Qualifier("contextMapperProperties") MapperProperties mapperProperties) {
    return new MapperHandler(mapperProperties);
  }

  @Bean
  public ApplicationListener<EnvironmentChangeEvent> governanceApplicationListener() {
    return environmentChangeEvent -> GovernanceEventManager
        .post(new GovernanceConfigurationChangedEvent(environmentChangeEvent.getKeys()));
  }

  @Bean
  public MicroserviceMeta governanceMicroserviceMeta() {
    return new SpringCloudMicroserviceMeta();
  }

  @Bean
  public AbstractRetryExtension governanceRetryExtension(List<StatusCodeExtractor> statusCodeExtractors) {
    return new SpringCloudRetryExtension(statusCodeExtractors);
  }

  @Bean
  public AbstractCircuitBreakerExtension circuitBreakerExtension(List<StatusCodeExtractor> statusCodeExtractors) {
    return new SpringCloudCircuitBreakerExtension(statusCodeExtractors);
  }

  @Bean
  public AbstractInstanceIsolationExtension instanceIsolationExtension(List<StatusCodeExtractor> statusCodeExtractors) {
    return new SpringCloudInstanceIsolationExtension(statusCodeExtractors);
  }
}
