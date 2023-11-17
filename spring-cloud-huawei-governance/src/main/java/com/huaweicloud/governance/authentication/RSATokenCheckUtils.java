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

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.foundation.common.utils.KeyPairUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.huaweicloud.common.context.InvocationContextHolder;

import javax.servlet.http.HttpServletRequest;

public class RSATokenCheckUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(RSATokenCheckUtils.class);

  public static final Cache<RsaAuthenticationToken, Boolean> validatedToken = CacheBuilder.newBuilder()
      .expireAfterAccess(getExpiredTime(), TimeUnit.MILLISECONDS)
      .build();

  public static RsaAuthenticationToken checkTokenInfo(HttpServletRequest request,
      AuthenticationAdapter authenticationAdapter, String headerTokenKey) throws Exception {
    String token = request.getHeader(headerTokenKey);
    if (StringUtils.isEmpty(token)) {
      token = InvocationContextHolder.getOrCreateInvocationContext().getContext(Const.AUTH_TOKEN);
    }
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
    if (validatedToken.asMap().containsKey(rsaToken)) {
      return rsaToken;
    }
    if (isValidToken(rsaToken, authenticationAdapter)) {
      validatedToken.put(rsaToken, true);
    } else {
      LOGGER.error("token authentication failed, check token info.");
      throw new UnAuthorizedException("UNAUTHORIZED.");
    }
    return rsaToken;
  }

  public static boolean tokenExpired(RsaAuthenticationToken rsaToken) {
    long generateTime = rsaToken.getGenerateTime();
    long expired = generateTime + RsaAuthenticationToken.TOKEN_ACTIVE_TIME + 15 * 60 * 1000;
    long now = System.currentTimeMillis();
    return now > expired;
  }

  public static boolean isValidToken(RsaAuthenticationToken rsaToken,
      AuthenticationAdapter adapter) throws Exception {
    String sign = rsaToken.getSign();
    String content = rsaToken.plainToken();
    String publicKey = adapter.getPublicKeyFromInstance(rsaToken.getInstanceId(), rsaToken.getServiceId());
    return KeyPairUtils.verify(publicKey, sign, content);
  }

  private static int getExpiredTime() {
    return 60 * 60 * 1000;
  }
}
