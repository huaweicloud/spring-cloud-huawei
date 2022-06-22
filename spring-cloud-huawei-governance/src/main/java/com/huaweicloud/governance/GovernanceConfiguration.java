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
package com.huaweicloud.governance;

import java.util.HashSet;
import java.util.List;

import org.apache.servicecomb.governance.InvocationContext;
import org.apache.servicecomb.governance.MicroserviceMeta;
import org.apache.servicecomb.governance.event.GovernanceConfigurationChangedEvent;
import org.apache.servicecomb.governance.event.GovernanceEventManager;
import org.apache.servicecomb.governance.handler.ext.RetryExtension;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.event.ConfigRefreshEvent;

@Configuration
@ComponentScan(basePackages = {"org.apache.servicecomb.governance"})
public class GovernanceConfiguration {
  @Bean
  public ApplicationListener<ConfigRefreshEvent> governanceApplicationListener() {
    return configRefreshEvent -> GovernanceEventManager
        .post(new GovernanceConfigurationChangedEvent(new HashSet<>(configRefreshEvent.getChange())));
  }

  @Bean
  public MicroserviceMeta governanceMicroserviceMeta() {
    return new SpringCloudMicroserviceMeta();
  }

  @Bean
  public InvocationContext governanceInvocationContext() {
    return new SpringCloudInvocationContext();
  }

  @Bean
  public RetryExtension governanceRetryExtension(List<StatusCodeExtractor> statusCodeExtractors) {
    return new SpringCloudRetryExtension(statusCodeExtractors);
  }
}
