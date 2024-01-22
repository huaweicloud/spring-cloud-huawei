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

package com.huaweicloud.service.engine.common;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.huaweicloud.service.engine.common.configration.bootstrap.BootstrapProperties;
import com.huaweicloud.service.engine.common.configration.bootstrap.ServiceCombAkSkProperties;
import com.huaweicloud.service.engine.common.transport.AkSkRequestAuthHeaderProvider;
import com.huaweicloud.service.engine.common.transport.RBACRequestAuthHeaderProvider;

@Configuration
public class EngineCommonConfiguration {
  @Bean
  public AuthHeaderProvider akSkRequestAuthHeaderProvider(ServiceCombAkSkProperties serviceCombAkSkProperties) {
    return new AkSkRequestAuthHeaderProvider(serviceCombAkSkProperties);
  }

  @Bean
  public AuthHeaderProvider rbacRequestAuthHeaderProvider(BootstrapProperties bootstrapProperties) {
    return new RBACRequestAuthHeaderProvider(bootstrapProperties);
  }
}
