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

package com.huaweicloud.governance.adapters.webflux;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.springframework.web.server.ServerWebExchange;

public final class WebFluxUtils {
  private WebFluxUtils() {

  }

  /**
   * Create GovernanceRequest from ServerWebExchange.
   * In gateway, do not have serviceName and instanceId information.
   *
   * TODO: WebFlux support service name and instance id, distinguish gateway and microservice. 
   */
  public static GovernanceRequestExtractor createProviderGovernanceRequest(ServerWebExchange exchange) {
    return new GovernanceRequestExtractor() {

      @Override
      public String apiPath() {
        return exchange.getRequest().getURI().getPath();
      }

      @Override
      public String method() {
        return exchange.getRequest().getMethod().name();
      }

      @Override
      public String header(String key) {
        return exchange.getRequest().getHeaders().getFirst(key);
      }

      @Override
      public String instanceId() {
        return null;
      }

      @Override
      public String serviceName() {
        return null;
      }

      @Override
      public Object sourceRequest() {
        return exchange;
      }
    };
  }
}
