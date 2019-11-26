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
package com.huaweicloud.dtm;

import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import com.huawei.paas.dtm.client.config.ConfigItems;

/**
 * @Author wangqijun
 * @Date 20:04 2019-09-09
 **/

@Configuration
@ConditionalOnProperty(value = "dtm", matchIfMissing = true)
@AutoConfigureAfter(name = {
    "org.springframework.cloud.servicecomb.discovery.registry.ServiceCombRegistryAutoConfiguration"})
@ComponentScan(basePackages = {"com.huawei.middleware.dtm.client",})
public class DtmClientConfiguration {

  @Bean
  public ConfigItems configItems() {
    return new ConfigItems();
  }
}

