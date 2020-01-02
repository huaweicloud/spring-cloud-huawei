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

package com.huaweicloud.config;

import org.aspectj.lang.annotation.Aspect;
import org.springframework.boot.autoconfigure.aop.AopAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import com.huaweicloud.common.transport.ServiceCombAkSkProperties;
import org.springframework.cloud.context.refresh.ContextRefresher;
import com.huaweicloud.config.client.RefreshRecord;
import com.huaweicloud.config.client.ServiceCombConfigClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.retry.interceptor.RetryInterceptorBuilder;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;
import org.springframework.retry.interceptor.Retryable;

/**
 * @Author wangqijun
 * @Date 11:23 2019-10-17
 **/
@Configuration
@ConditionalOnServiceCombEnabled
@ConditionalOnProperty(name = "spring.cloud.servicecomb.config.enabled", matchIfMissing = true)
public class ServiceCombConfigAutoConfiguration {

  @Bean
  @ConditionalOnMissingBean
  public ServiceCombConfigProperties serviceCombConfigProperties() {
    return new ServiceCombConfigProperties();
  }

  @Configuration
  protected static class RefreshConfiguration {
    @Bean
    @ConditionalOnProperty(name = "spring.cloud.servicecomb.config.watch.enabled",
        matchIfMissing = true)
    public RefreshRecord refreshRecord() {
      return new RefreshRecord();
    }


    @Bean
    @ConditionalOnProperty(name = "spring.cloud.servicecomb.config.watch.enabled",
        matchIfMissing = true)
    public ConfigWatch configWatch(ServiceCombConfigProperties serviceCombConfigProperties,
        ServiceCombConfigClient serviceCombConfigClient,
        ContextRefresher contextRefresher, RefreshRecord refreshRecord,
        ServiceCombAkSkProperties serviceCombAkSkProperties) {
      ConfigWatch watch = new ConfigWatch();
      watch.setProject(serviceCombAkSkProperties.getProject());
      watch.setContextRefresher(contextRefresher);
      watch.setServiceCombConfigClient(serviceCombConfigClient);
      watch.setServiceCombConfigProperties(serviceCombConfigProperties);
      watch.setRefreshRecord(refreshRecord);
      return watch;
    }
  }

  @ConditionalOnClass({Retryable.class, Aspect.class, AopAutoConfiguration.class})
  @Configuration
  @EnableRetry(proxyTargetClass = true)
  @Import(AopAutoConfiguration.class)
  @ConditionalOnProperty(name = "spring.cloud.servicecomb.config.retry.enabled",
      matchIfMissing = true)
  protected static class RetryConfiguration {

    @Bean(name = "serviceCombConfigRetryInterceptor")
    @ConditionalOnMissingBean(name = "serviceCombConfigRetryInterceptor")
    public RetryOperationsInterceptor serviceCombConfigRetryInterceptor(
        ServiceCombConfigProperties serviceCombConfigProperties) {
      return RetryInterceptorBuilder.stateless()
          .backOffOptions(serviceCombConfigProperties.getRetry().getInitialInterval(),
              serviceCombConfigProperties.getRetry().getMultiplier(),
              serviceCombConfigProperties.getRetry().getMaxInterval())
          .maxAttempts(serviceCombConfigProperties.getRetry().getMaxAttempts()).build();
    }
  }
}
