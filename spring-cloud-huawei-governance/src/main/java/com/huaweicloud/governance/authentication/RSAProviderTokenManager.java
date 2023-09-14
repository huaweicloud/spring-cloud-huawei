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

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.servicecomb.foundation.common.utils.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class RSAProviderTokenManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(RSAProviderTokenManager.class);

  private final Cache<RsaAuthenticationToken, Boolean> validatedToken = CacheBuilder.newBuilder()
      .expireAfterAccess(getExpiredTime(), TimeUnit.MILLISECONDS)
      .build();

  private final List<AccessController> accessControllers;

  public RSAProviderTokenManager(List<AccessController> accessControllers) {
    this.accessControllers = accessControllers;
  }

  public void valid(String token, Map<String, String> requestMap) throws Exception {
    try {
      if (null == token) {
        LOGGER.error("token is null, perhaps you need to set auth handler at consumer");
        throw new UnAuthorizedException("UNAUTHORIZED.");
      }
      RsaAuthenticationToken rsaToken = RsaAuthenticationToken.fromStr(token);
      if (null == rsaToken) {
        LOGGER.error("token format is error, perhaps you need to set auth handler at consumer");
        throw new UnAuthorizedException("UNAUTHORIZED.");
      }
      if (tokenExpired(rsaToken)) {
        LOGGER.error("token is expired");
        throw new UnAuthorizedException("UNAUTHORIZED.");
      }
      boolean isAllow = true;
      for (AccessController accessController : accessControllers) {
        if (validatedToken.asMap().containsKey(rsaToken)) {
          isAllow = accessController.isAllowed(rsaToken.getServiceId(), rsaToken.getInstanceId(), requestMap);
        } else if (isValidToken(rsaToken, accessController) && !tokenExpired(rsaToken)) {
          validatedToken.put(rsaToken, true);
          isAllow = accessController.isAllowed(rsaToken.getServiceId(), rsaToken.getInstanceId(), requestMap);
        }
        if (!isAllow) {
          throw new UnAuthorizedException(accessController.interceptMessage());
        }
      }
    } catch (InvalidKeyException | NoSuchAlgorithmException | InvalidKeySpecException | SignatureException e) {
      LOGGER.error("verify error", e);
    }
  }

  public boolean isValidToken(RsaAuthenticationToken rsaToken, AccessController accessController)
      throws NoSuchAlgorithmException, InvalidKeySpecException, InvalidKeyException, SignatureException {
    String sign = rsaToken.getSign();
    String content = rsaToken.plainToken();
    String publicKey = accessController.getPublicKeyFromInstance(rsaToken.getInstanceId(), rsaToken.getServiceId());
    return KeyPairUtils.verify(publicKey, sign, content);
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
