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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.common.configration.dynamic.GovernanceProperties;
import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.UnAuthorizedException;

import reactor.core.publisher.Mono;

public class WebFluxProviderAuthFilter implements OrderedWebFilter {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebFluxProviderAuthFilter.class);

  private final WebFluxRSAProviderAuthManager webFluxRSAProviderAuthManager;

  public WebFluxProviderAuthFilter(List<AccessController> accessControllers, Environment environment,
      AuthenticationAdapter authenticationAdapter) {
    webFluxRSAProviderAuthManager = new WebFluxRSAProviderAuthManager(accessControllers, environment, authenticationAdapter);
  }

  @Override
  public int getOrder() {
    return GovernanceProperties.WEB_FILTER_SERVICE_AUTH_ORDER;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    try {
      if (webFluxRSAProviderAuthManager.isRequiredAuth(exchange.getRequest().getURI().getPath())) {
        webFluxRSAProviderAuthManager.valid(exchange);
      }
    } catch (Exception e) {
      if (e instanceof UnAuthorizedException) {
        LOGGER.warn("authentication failed: {}", e.getMessage());
        return Mono.error(new ResponseStatusException(HttpStatus.FORBIDDEN, e.getMessage(), e));
      } else {
        return Mono.error(new RuntimeException(e));
      }
    }
    return chain.filter(exchange);
  }
}
