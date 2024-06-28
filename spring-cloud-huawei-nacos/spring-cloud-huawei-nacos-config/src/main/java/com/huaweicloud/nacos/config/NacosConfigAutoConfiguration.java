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

package com.huaweicloud.nacos.config;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.huaweicloud.nacos.config.manager.NacosConfigServiceManager;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceMasterManager;
import com.huaweicloud.nacos.config.manager.NacosConfigServiceStandbyManager;
import com.huaweicloud.nacos.config.refresh.NacosConfigContextRefresher;

@Configuration(proxyBeanMethods = false)
@ConditionalOnNacosConfigEnabled
public class NacosConfigAutoConfiguration {
  @Bean
  @ConditionalOnMissingBean
  public NacosConfigProperties nacosConfigProperties() {
    return new NacosConfigProperties();
  }

  @Bean
  @ConditionalOnMissingBean
  public NacosConfigServiceMasterManager nacosConfigServiceManagerMaster(NacosConfigProperties properties) {
    return new NacosConfigServiceMasterManager(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  @ConditionalOnProperty(value = NacosConfigProperties.PREFIX + ".master-standby-enabled", havingValue = "true")
  public NacosConfigServiceStandbyManager nacosConfigServiceManagerStandby(NacosConfigProperties properties) {
    return new NacosConfigServiceStandbyManager(properties);
  }

  @Bean
  @ConditionalOnMissingBean
  public NacosConfigContextRefresher nacosConfigContextRefresher(NacosConfigProperties configProperties,
      List<NacosConfigServiceManager> configServiceManagers, Environment env) {
    return new NacosConfigContextRefresher(configProperties, env, configServiceManagers);
  }
}
