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

package com.huaweicloud.governance.adapters.webmvc;

import jakarta.servlet.http.HttpServletRequest;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

public class WebMvcUtils {
  private WebMvcUtils() {

  }

  /**
   * Create a GovernanceRequest from HttpServletRequest
   */
  public static GovernanceRequestExtractor convert(HttpServletRequest request) {
    return new GovernanceRequestExtractor() {
      @Override
      public String apiPath() {
        return request.getRequestURI();
      }

      @Override
      public String method() {
        return request.getMethod();
      }

      @Override
      public String header(String key) {
        return request.getHeader(key);
      }

      @Override
      public String instanceId() {
        return null;
      }

      @Override
      public String serviceName() {
        return InvocationContextHolder
            .getOrCreateInvocationContext().getContext(InvocationContext.CONTEXT_MICROSERVICE_NAME);
      }

      @Override
      public Object sourceRequest() {
        return request;
      }
    };
  }
}
