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

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.env.Environment;

import com.huaweicloud.governance.StatusCodeExtractor;
import com.huaweicloud.governance.adapters.GovernanceHeaderStatusUtils;

import jakarta.servlet.http.HttpServletResponse;

public class HttpServletResponseStatusCodeExtractor implements StatusCodeExtractor {
  private final Environment environment;

  public HttpServletResponseStatusCodeExtractor(Environment environment) {
    this.environment = environment;
  }

  @Override
  public boolean canProcess(Object response) {
    return response instanceof HttpServletResponse;
  }

  @Override
  public String extractStatusCode(Object response) {
    String statusHeaderKey = GovernanceHeaderStatusUtils.getStatusHeaderKey(environment);
    if (!StringUtils.isEmpty(((HttpServletResponse) response).getHeader(statusHeaderKey))) {
      return ((HttpServletResponse) response).getHeader(statusHeaderKey);
    }
    return String.valueOf(((HttpServletResponse) response).getStatus());
  }
}