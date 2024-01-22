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

package com.huaweicloud.governance.adapters.loadbalancer;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.DefaultRequestContext;
import org.springframework.cloud.client.loadbalancer.Request;
import org.springframework.cloud.client.loadbalancer.RequestData;

public final class LoadBalancerUtils {
  private static final Logger LOGGER = LoggerFactory.getLogger(LoadBalancerUtils.class);

  private LoadBalancerUtils() {

  }

  @SuppressWarnings("rawtypes")
  public static GovernanceRequestExtractor convert(Request request, String serviceId) {
    Object context = request.getContext();
    if (context instanceof DefaultRequestContext) {
      Object clientRequest = ((DefaultRequestContext) context).getClientRequest();
      if (clientRequest instanceof RequestData) {
        RequestData requestData = (RequestData) clientRequest;
        return new GovernanceRequestExtractor() {
          @Override
          public String apiPath() {
            return requestData.getUrl().getPath();
          }

          @Override
          public String method() {
            return requestData.getHttpMethod().name();
          }

          @Override
          public String header(String key) {
            return requestData.getHeaders().getFirst(key);
          }

          @Override
          public String instanceId() {
            return null;
          }

          @Override
          public String serviceName() {
            return serviceId;
          }

          @Override
          public Object sourceRequest() {
            return requestData;
          }
        };
      } else if (clientRequest instanceof DecorateLoadBalancerRequest) {
        DecorateLoadBalancerRequest requestData = (DecorateLoadBalancerRequest) clientRequest;
        return new GovernanceRequestExtractor() {
          @Override
          public String apiPath() {
            return requestData.getRequest().getURI().getPath();
          }

          @Override
          public String method() {
            return requestData.getRequest().getMethod().name();
          }

          @Override
          public String header(String key) {
            return requestData.getRequest().getHeaders().getFirst(key);
          }

          @Override
          public String instanceId() {
            return null;
          }

          @Override
          public String serviceName() {
            return serviceId;
          }

          @Override
          public Object sourceRequest() {
            return requestData;
          }
        };
      } else {
        LOGGER.warn("not implemented client request {}.", clientRequest == null ? null : clientRequest.getClass());
      }
    } else {
      LOGGER.warn("not implemented context {}.", context == null ? null : context.getClass());
    }
    return null;
  }
}
