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
package com.huaweicloud.governance.authentication;

import org.apache.servicecomb.foundation.common.utils.RSAKeyPairEntry;
import org.apache.servicecomb.foundation.common.utils.RSAUtils;
import org.apache.servicecomb.foundation.token.RSAKeypair4Auth;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

import com.huaweicloud.servicecomb.discovery.registry.ServiceCombRegistration;

/**
 *
 * initialize public and private key pair when system boot before registry instance to service center
 *
 *
 */
public class AuthHandlerBoot implements ApplicationListener<ApplicationEvent> {

  private final ServiceCombRegistration registration;

  public AuthHandlerBoot(ServiceCombRegistration registration) {
    this.registration = registration;
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (event instanceof InstancePreRegisteredEvent) {

      RSAKeyPairEntry rsaKeyPairEntry = RSAUtils.generateRSAKeyPair();
      RSAKeypair4Auth.INSTANCE.setPrivateKey(rsaKeyPairEntry.getPrivateKey());
      RSAKeypair4Auth.INSTANCE.setPublicKey(rsaKeyPairEntry.getPublicKey());
      RSAKeypair4Auth.INSTANCE.setPublicKeyEncoded(rsaKeyPairEntry.getPublicKeyEncoded());
      registration.getMicroserviceInstance().getProperties().put(Const.INSTANCE_PUBKEY_PRO,
          rsaKeyPairEntry.getPublicKeyEncoded());
    }
  }
}
