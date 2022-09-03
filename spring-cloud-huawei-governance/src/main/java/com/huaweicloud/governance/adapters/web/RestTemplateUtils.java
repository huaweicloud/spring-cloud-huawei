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

import org.apache.servicecomb.governance.marker.GovernanceRequest;
import org.springframework.http.HttpRequest;

import com.huaweicloud.common.adapters.loadbalancer.RetryContext;
import com.huaweicloud.common.context.InvocationContextHolder;

public final class RestTemplateUtils {
  private RestTemplateUtils() {

  }

  public static GovernanceRequest createGovernanceRequest(HttpRequest request) {
    GovernanceRequest governanceRequest = new GovernanceRequest();
    governanceRequest.setUri(request.getURI().getPath());
    governanceRequest.setMethod(request.getMethod().name());
    governanceRequest.setHeaders(request.getHeaders().toSingleValueMap());

    RetryContext retryContext = InvocationContextHolder.getOrCreateInvocationContext()
        .getLocalContext(RetryContext.RETRY_CONTEXT);
    if (retryContext != null && retryContext.getLastServer() != null) {
      governanceRequest.setServiceName(retryContext.getLastServer().getServiceId());
      governanceRequest.setInstanceId(retryContext.getLastServer().getInstanceId());
    }
    return governanceRequest;
  }
}
