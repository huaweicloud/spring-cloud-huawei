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

package com.huaweicloud.common.configration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.common.configration.dynamic.ContextProperties;
import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.common.configration.dynamic.HttpClientProperties;
import com.huaweicloud.common.configration.dynamic.LoadBalancerProperties;
import com.huaweicloud.common.configration.dynamic.MetricsProperties;

@Configuration
public class DynamicPropertiesConfiguration {
  @Bean
  @RefreshScope
  @ConfigurationProperties("spring.cloud.servicecomb.metrics")
  public MetricsProperties metricsProperties() {
    return new MetricsProperties();
  }

  @Bean
  @RefreshScope
  @ConfigurationProperties("spring.cloud.servicecomb.context")
  public ContextProperties contextProperties() {
    return new ContextProperties();
  }

  @Bean
  @RefreshScope
  @ConfigurationProperties("spring.cloud.servicecomb.loadbalancer")
  public LoadBalancerProperties loadBalancerProperties() {
    return new LoadBalancerProperties();
  }

  @Bean
  @RefreshScope
  @ConfigurationProperties("spring.cloud.servicecomb.httpclient")
  public HttpClientProperties restTemplateHttpClientProperties() {
    return new HttpClientProperties();
  }


  @Bean
  @RefreshScope
  @ConfigurationProperties(GovernanceProperties.PREFIX)
  public GovernanceProperties governanceProperties() {
    return new GovernanceProperties();
  }
}
