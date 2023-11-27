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

package com.huaweicloud.governance.adapters.web;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.springframework.cloud.client.loadbalancer.ServiceRequestWrapper;
import org.springframework.http.HttpRequest;

import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.disovery.InstanceIDAdapter;
import com.huaweicloud.governance.adapters.loadbalancer.RetryContext;

public final class RestTemplateUtils {
  private RestTemplateUtils() {

  }

  public static GovernanceRequestExtractor createGovernanceRequest(HttpRequest request) {
    return new GovernanceRequestExtractor() {
      @Override
      public String apiPath() {
        return request.getURI().getPath();
      }

      @Override
      public String method() {
        return request.getMethod().name();
      }

      @Override
      public String header(String key) {
        return request.getHeaders().getFirst(key);
      }

      @Override
      public String instanceId() {
        RetryContext retryContext = InvocationContextHolder.getOrCreateInvocationContext()
            .getLocalContext(RetryContext.RETRY_CONTEXT);
        if (retryContext != null && retryContext.getLastServer() != null) {
          return InstanceIDAdapter.instanceId(retryContext.getLastServer());
        }
        return null;
      }

      @Override
      public String serviceName() {
        if (request instanceof ServiceRequestWrapper) {
          return ((ServiceRequestWrapper) request).getRequest().getURI().getHost();
        }
        return request.getURI().getHost();
      }

      @Override
      public Object sourceRequest() {
        return request;
      }
    };
  }
}
