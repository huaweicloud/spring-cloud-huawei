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

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.governance.GovernanceConst;
import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthRequestExtractor;
import com.huaweicloud.governance.authentication.AuthRequestExtractorUtils;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.MatcherUtils;
import com.huaweicloud.governance.authentication.RSATokenCheckUtils;
import com.huaweicloud.governance.authentication.RsaAuthenticationToken;
import com.huaweicloud.governance.authentication.UnAuthorizedException;

public class WebFluxRSAProviderAuthManager {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebFluxRSAProviderAuthManager.class);

  private final List<AccessController> accessControllers;

  private final Environment environment;

  private final AuthenticationAdapter authenticationAdapter;

  public WebFluxRSAProviderAuthManager(List<AccessController> accessControllers, Environment environment,
      AuthenticationAdapter authenticationAdapter) {
    this.accessControllers = accessControllers;
    this.environment = environment;
    this.authenticationAdapter = authenticationAdapter;
  }

  /**
   * 1.tokenCheckEnabled is true or request headers has no serviceName, use serviceId and instanceId for authentication
   * in token.
   * 2.tokenCheckEnabled is false and request headers has serviceName, use serviceName for authentication in request
   * header.
   *
   * @param exchange
   * @throws Exception
   */
  public void valid(ServerWebExchange exchange) throws Exception {
    try {
      AuthRequestExtractor extractor;
      // only allow outside request using set serviceName and token to authentication,
      // between microservice use  InvocationContext.
      if (environment.getProperty(GovernanceConst.AUTH_TOKEN_CHECK_ENABLED, boolean.class, true)
          || StringUtils.isEmpty(exchange.getRequest().getHeaders().getFirst(GovernanceConst.AUTH_SERVICE_NAME))) {
        String headerTokenKey = environment.getProperty(GovernanceConst.AUTH_TOKEN_HEADER_KEY, String.class,
            "X-SM-Token");
        String requestHeadToken = exchange.getRequest().getHeaders().getFirst(headerTokenKey);
        InvocationContext invocationContext =
            (InvocationContext) exchange.getAttributes().get(InvocationContextHolder.ATTRIBUTE_KEY);
        RsaAuthenticationToken rsaToken = RSATokenCheckUtils.checkTokenInfo(authenticationAdapter, requestHeadToken,
            invocationContext);
        extractor = AuthRequestExtractorUtils.createWebFluxAuthRequestExtractor(exchange, rsaToken.getServiceId(),
            rsaToken.getInstanceId());
      } else {
        extractor = AuthRequestExtractorUtils.createWebFluxAuthRequestExtractor(exchange, "", "");
      }
      for (AccessController accessController : accessControllers) {
        if (!accessController.isAllowed(extractor)) {
          throw new UnAuthorizedException(accessController.interceptMessage());
        }
      }
    } catch(Exception e) {
      LOGGER.error("verify error", e);
      throw e;
    }
  }

  public boolean checkUriWhitelist(String uri) {
    return MatcherUtils.isMatchUriWhitelist(uri, environment);
  }
}
