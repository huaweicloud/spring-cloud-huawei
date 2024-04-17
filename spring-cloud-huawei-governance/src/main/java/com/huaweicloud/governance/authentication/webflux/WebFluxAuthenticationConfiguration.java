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

package com.huaweicloud.governance.authentication.webflux;

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.server.WebFilter;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;

@Configuration
@ConditionalOnWebApplication(type = Type.REACTIVE)
public class WebFluxAuthenticationConfiguration {
  @Bean
  @ConditionalOnExpression("${" + GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED + ":false}"
      + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_SECURITY_POLICY_ENABLED + ":false}")
  public WebFilter webFluxProviderAuthFilter(List<AccessController> accessController,
      Environment environment, AuthenticationAdapter authenticationAdapter) {
    return new WebFluxProviderAuthFilter(accessController, environment, authenticationAdapter);
  }
}
