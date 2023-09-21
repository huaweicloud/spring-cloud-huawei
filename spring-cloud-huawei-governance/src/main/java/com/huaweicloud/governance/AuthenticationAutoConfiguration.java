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

import java.util.List;

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import com.huaweicloud.common.configration.dynamic.BlackWhiteListProperties;
import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthHandlerBoot;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.consumer.RSAConsumerTokenManager;
import com.huaweicloud.governance.authentication.whiteBlack.WhiteBlackAccessController;
import com.huaweicloud.governance.authentication.ProviderAuthPreHandlerInterceptor;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyAccessController;
import com.huaweicloud.governance.authentication.securityPolicy.SecurityPolicyProperties;

@Configuration
@ConditionalOnExpression("${" + GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED + ":false}"
    + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED + ":false}"
    + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_SECURITY_POLICY_ENABLED + ":false}")
public class AuthenticationAutoConfiguration {
  @Bean
  @RefreshScope
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED,
      havingValue = "true")
  @ConfigurationProperties(GovernanceProperties.WEBMVC_PUBLICKEY_ACCSSCONTROL)
  public BlackWhiteListProperties blackWhiteListProperties() {
    return new BlackWhiteListProperties();
  }

  @Bean
  @ConditionalOnExpression("${" + GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED + ":true}"
      + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED + ":true}"
      + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_SECURITY_POLICY_ENABLED + ":true}")
  public ApplicationListener<ApplicationEvent> authHandlerBoot(Registration registration,
      AuthenticationAdapter adapter) {
    return new AuthHandlerBoot(registration, adapter);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_PUBLICKEY_CONSUMER_ENABLED,
      havingValue = "true")
  public RSAConsumerTokenManager authenticationTokenManager(Registration instanceService,
      AuthenticationAdapter adapter) {
    return new RSAConsumerTokenManager(instanceService, adapter);
  }

  @Bean
  @ConditionalOnExpression("${" + GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED + ":false}"
      + " or ${" + GovernanceProperties.WEBMVC_PUBLICKEY_SECURITY_POLICY_ENABLED + ":false}")
  public ProviderAuthPreHandlerInterceptor providerAuthPreHandlerInterceptor(List<AccessController> accessController) {
    return new ProviderAuthPreHandlerInterceptor(accessController);
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_PUBLICKEY_PROVIDER_ENABLED,
      havingValue = "true")
  public WhiteBlackAccessController whiteBlackAccessController(AuthenticationAdapter authenticationAdapter,
      BlackWhiteListProperties blackWhiteListProperties, Environment environment) {
    return new WhiteBlackAccessController(authenticationAdapter, blackWhiteListProperties, environment);
  }

  @Bean
  @RefreshScope
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_PUBLICKEY_SECURITY_POLICY_ENABLED,
      havingValue = "true")
  @ConfigurationProperties(GovernanceProperties.WEBMVC_PUBLICKEY_ACLS)
  public SecurityPolicyProperties securityPolicyProperties() {
    return new SecurityPolicyProperties();
  }

  @Bean
  @ConditionalOnProperty(value = GovernanceProperties.WEBMVC_PUBLICKEY_SECURITY_POLICY_ENABLED,
      havingValue = "true")
  public SecurityPolicyAccessController securityPolicyAccessController(AuthenticationAdapter authenticationAdapter,
      SecurityPolicyProperties securityPolicyProperties, Environment environment) {
    return new SecurityPolicyAccessController(authenticationAdapter, securityPolicyProperties, environment);
  }
}
