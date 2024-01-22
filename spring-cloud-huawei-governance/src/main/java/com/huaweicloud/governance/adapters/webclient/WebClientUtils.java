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

package com.huaweicloud.governance.adapters.webclient;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.web.reactive.function.client.ClientRequest;

import com.huaweicloud.common.disovery.InstanceIDAdapter;
import com.huaweicloud.governance.adapters.loadbalancer.RetryContext;

public final class WebClientUtils {
  private WebClientUtils() {

  }

  public static GovernanceRequestExtractor createGovernanceRequest(ClientRequest request) {
    return new GovernanceRequestExtractor() {
      @Override
      public String apiPath() {
        return request.url().getPath();
      }

      @Override
      public String method() {
        return request.method().name();
      }

      @Override
      public String header(String key) {
        return request.headers().getFirst(key);
      }

      @Override
      public String instanceId() {
        ServiceInstance serviceInstance = (ServiceInstance) request.attributes()
            .get(RetryContext.RETRY_SERVICE_INSTANCE);
        if (serviceInstance != null) {
          return InstanceIDAdapter.instanceId(serviceInstance);
        }
        return null;
      }

      @Override
      public String serviceName() {
        ServiceInstance serviceInstance = (ServiceInstance) request.attributes()
            .get(RetryContext.RETRY_SERVICE_INSTANCE);
        if (serviceInstance != null) {
          return serviceInstance.getServiceId();
        }
        return null;
      }

      @Override
      public Object sourceRequest() {
        return request;
      }
    };
  }
}
