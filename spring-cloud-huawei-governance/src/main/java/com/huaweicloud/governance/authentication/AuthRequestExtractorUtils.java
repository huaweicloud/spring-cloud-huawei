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

package com.huaweicloud.governance.authentication;


import org.apache.commons.lang.StringUtils;

import com.huaweicloud.common.context.InvocationContextHolder;

import jakarta.servlet.http.HttpServletRequest;

public class AuthRequestExtractorUtils {
  public static AuthRequestExtractor createAuthRequestExtractor(HttpServletRequest request, String serviceId,
      String instanceId) {
    return new AuthRequestExtractor() {
      @Override
      public String uri() {
        return request.getRequestURI();
      }

      @Override
      public String method() {
        return request.getMethod();
      }

      @Override
      public String instanceId() {
        return instanceId;
      }

      @Override
      public String serviceName() {
        String serviceName = request.getHeader(Const.AUTH_SERVICE_NAME);
        if (StringUtils.isEmpty(serviceName)) {
          serviceName = InvocationContextHolder.getOrCreateInvocationContext().getContext(Const.AUTH_SERVICE_NAME);
        }
        return serviceName;
      }

      @Override
      public String serviceId() {
        return serviceId;
      }
    };
  }
}
