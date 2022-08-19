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

package com.huaweicloud.governance.adapters.gateway;

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

public final class GatewayUtils {
  private GatewayUtils() {

  }

  /**
   * Create GovernanceRequest from ServerWebExchange.
   * In gateway, do not have serviceName and instanceId information.
   */
  public static GovernanceRequest createProviderGovernanceRequest(ServerWebExchange exchange) {
    GovernanceRequest request = new GovernanceRequest();
    request.setHeaders(exchange.getRequest().getHeaders().toSingleValueMap());
    request.setMethod(exchange.getRequest().getMethodValue());
    request.setUri(exchange.getRequest().getURI().getPath());
    return request;
  }

  /**
   * Create GovernanceRequest from ServerWebExchange.
   * In gateway, after ReactiveLoadBalancerClientFilter we can get target service name and instance id.
   */
  public static GovernanceRequest createConsumerGovernanceRequest(ServerWebExchange exchange) {
    GovernanceRequest request = new GovernanceRequest();
    request.setHeaders(exchange.getRequest().getHeaders().toSingleValueMap());
    request.setMethod(exchange.getRequest().getMethodValue());
    request.setUri(exchange.getRequest().getURI().getPath());

    Response<ServiceInstance> response = exchange.getAttribute(
        ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
    if (response != null && response.hasServer()) {
      request.setServiceName(response.getServer().getServiceId());
      request.setInstanceId(response.getServer().getInstanceId());
    }
    return request;
  }
}
