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
package com.huaweicloud.governance.authentication;

import org.apache.servicecomb.foundation.common.utils.KeyPairEntry;
import org.apache.servicecomb.foundation.common.utils.KeyPairUtils;
import org.apache.servicecomb.foundation.token.Keypair4Auth;
import org.springframework.cloud.client.discovery.event.InstancePreRegisteredEvent;
import org.springframework.cloud.client.serviceregistry.Registration;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationListener;

/**
 *
 * initialize public and private key pair when system boot before registry instance to service center
 *
 *
 */
public class AuthHandlerBoot implements ApplicationListener<ApplicationEvent> {

  private final Registration registration;

  private final AuthenticationAdapter adapter;

  public AuthHandlerBoot(Registration registration, AuthenticationAdapter adapter) {
    this.registration = registration;
    this.adapter = adapter;
  }

  @Override
  public void onApplicationEvent(ApplicationEvent event) {
    if (event instanceof InstancePreRegisteredEvent) {
      KeyPairEntry rsaKeyPairEntry = KeyPairUtils.generateRSAKeyPair();
      Keypair4Auth.INSTANCE.setPrivateKey(rsaKeyPairEntry.getPrivateKey());
      Keypair4Auth.INSTANCE.setPublicKey(rsaKeyPairEntry.getPublicKey());
      Keypair4Auth.INSTANCE.setPublicKeyEncoded(rsaKeyPairEntry.getPublicKeyEncoded());
      adapter.setPublicKey(registration, rsaKeyPairEntry.getPublicKeyEncoded());
    }
  }
}
