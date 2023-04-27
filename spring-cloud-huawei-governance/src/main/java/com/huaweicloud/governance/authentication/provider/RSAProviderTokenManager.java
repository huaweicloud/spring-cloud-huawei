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

package com.huaweicloud.governance.authentication.provider;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huaweicloud.governance.authentication.RsaAuthenticationToken;
import org.apache.servicecomb.foundation.common.utils.RSAUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.concurrent.TimeUnit;

public class RSAProviderTokenManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSAProviderTokenManager.class);

  private final Cache<RsaAuthenticationToken, Boolean> validatedToken = CacheBuilder.newBuilder()
      .expireAfterAccess(getExpiredTime(), TimeUnit.MILLISECONDS)
      .build();

  private final AccessController accessController;

  public RSAProviderTokenManager(AccessController accessController) {
    this.accessController = accessController;
  }

  public boolean valid(String token) {
    try {
      RsaAuthenticationToken rsaToken = RsaAuthenticationToken.fromStr(token);
      if (null == rsaToken) {
        LOGGER.error("token format is error, perhaps you need to set auth handler at consumer");
        return false;
      }
      if (tokenExpired(rsaToken)) {
        LOGGER.error("token is expired");
        return false;
      }

      if (validatedToken.asMap().containsKey(rsaToken)) {
        return accessController.isAllowed(rsaToken.getServiceId(), rsaToken.getInstanceId());
      }

      if (isValidToken(rsaToken) && !tokenExpired(rsaToken)) {
        validatedToken.put(rsaToken, true);
        return accessController.isAllowed(rsaToken.getServiceId(), rsaToken.getInstanceId());
      }
      return false;
    } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
      LOGGER.error("verify error", e);
      return false;
    }
  }

  public boolean isValidToken(RsaAuthenticationToken rsaToken)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    String sign = rsaToken.getSign();
    String content = rsaToken.plainToken();
    String publicKey = accessController.getPublicKeyFromInstance(rsaToken.getInstanceId(), rsaToken.getServiceId());
    return RSAUtils.verify(publicKey, sign, content);
  }

  protected int getExpiredTime() {
    return 60 * 60 * 1000;
  }

  private boolean tokenExpired(RsaAuthenticationToken rsaToken) {
    long generateTime = rsaToken.getGenerateTime();
    long expired = generateTime + RsaAuthenticationToken.TOKEN_ACTIVE_TIME + 15 * 60 * 1000;
    long now = System.currentTimeMillis();
    return now > expired;
  }

  @VisibleForTesting
  Cache<RsaAuthenticationToken, Boolean> getValidatedToken() {
    return validatedToken;
  }

}
