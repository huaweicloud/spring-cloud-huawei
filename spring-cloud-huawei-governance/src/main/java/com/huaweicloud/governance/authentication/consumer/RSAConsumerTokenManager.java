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

package com.huaweicloud.governance.authentication.consumer;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.governance.authentication.Const;
import com.huaweicloud.governance.authentication.MicroserviceInstanceService;
import com.huaweicloud.governance.authentication.RsaAuthenticationToken;
import com.huaweicloud.governance.authentication.UnAuthorizedException;
import org.apache.servicecomb.foundation.common.utils.RSAUtils;
import org.apache.servicecomb.foundation.token.RSAKeypair4Auth;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.PrivateKey;
import java.util.Optional;

public class RSAConsumerTokenManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSAConsumerTokenManager.class);

  private final Object lock = new Object();

  private RsaAuthenticationToken token;

  private final MicroserviceInstanceService instanceService;

  public RSAConsumerTokenManager(MicroserviceInstanceService instanceService) {
    this.instanceService = instanceService;
  }

  public String getToken() {

    if (isExpired(token)) {
      synchronized (lock) {
        if (isExpired(token)) {
          return createToken();
        }
      }
    }
    return token.format();
  }

  public String createToken() {
    PrivateKey privateKey = RSAKeypair4Auth.INSTANCE.getPrivateKey();
    String instanceId = instanceService.getInstanceId();
    String serviceId = instanceService.getServiceId();

    if (instanceId == null || serviceId == null) {
      LOGGER.error("service not ready when create token.");
      return null;
    }

    @SuppressWarnings("deprecation")
    String randomCode = org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric(128);
    long generateTime = System.currentTimeMillis();
    try {
      String plain = String.format("%s@%s@%s@%s", instanceId, serviceId, generateTime, randomCode);
      String sign = RSAUtils.sign(plain, privateKey);
      token = RsaAuthenticationToken.fromStr(String.format("%s@%s", plain, sign));
    } catch (Exception e) {
      LOGGER.error("create token error", e);
      return null;
    }
    return token.format();
  }

  /**
   * the TTL of Token is  24 hours
   * client token will expired 15 minutes early
   */
  public boolean isExpired(RsaAuthenticationToken token) {
    if (null == token) {
      return true;
    }
    long generateTime = token.getGenerateTime();
    long expiredDate = generateTime + RsaAuthenticationToken.TOKEN_ACTIVE_TIME - 15 * 60 * 1000;
    long now = System.currentTimeMillis();
    return now > expiredDate;
  }

  public void setToken(InvocationContext context) {
    Optional<String> token = Optional.ofNullable(getToken());
    if (!token.isPresent()) {
      throw new UnAuthorizedException("auth token is not properly configured yet.");
    }
    context.putContext(Const.AUTH_TOKEN, token.get());
  }
}
