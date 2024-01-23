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

package com.huaweicloud.governance.adapters.gateway;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.Response;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.web.server.ServerWebExchange;

import com.huaweicloud.common.disovery.InstanceIDAdapter;

public final class GatewayUtils {
  private GatewayUtils() {

  }

  /**
   * Create GovernanceRequest from ServerWebExchange.
   * In gateway, after ReactiveLoadBalancerClientFilter we can get target service name and instance id.
   */
  public static GovernanceRequestExtractor createConsumerGovernanceRequest(ServerWebExchange exchange) {
    return new GovernanceRequestExtractor() {
      @Override
      public String apiPath() {
        return exchange.getRequest().getURI().getPath();
      }

      @Override
      public String method() {
        return exchange.getRequest().getMethodValue();
      }

      @Override
      public String header(String key) {
        return exchange.getRequest().getHeaders().getFirst(key);
      }

      @Override
      public String instanceId() {
        Response<ServiceInstance> response = exchange.getAttribute(
            ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
        if (response != null && response.hasServer()) {
          return InstanceIDAdapter.instanceId(response.getServer());
        }
        return null;
      }

      @Override
      public String serviceName() {
        Response<ServiceInstance> response = exchange.getAttribute(
            ServerWebExchangeUtils.GATEWAY_LOADBALANCER_RESPONSE_ATTR);
        if (response != null && response.hasServer()) {
          return response.getServer().getServiceId();
        }
        return null;
      }

      @Override
      public Object sourceRequest() {
        return exchange;
      }
    };
  }
}
