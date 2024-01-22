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

package com.huaweicloud.governance.adapters.web;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.client.ClientHttpResponse;

import com.huaweicloud.governance.adapters.GovernanceHeaderStatusUtils;
import com.huaweicloud.governance.StatusCodeExtractor;

public class ClientHttpResponseStatusCodeExtractor implements StatusCodeExtractor {
  private static final Logger LOGGER = LoggerFactory.getLogger(ClientHttpResponseStatusCodeExtractor.class);

  private final Environment environment;

  public ClientHttpResponseStatusCodeExtractor(Environment environment) {
    this.environment = environment;
  }

  @Override
  public boolean canProcess(Object response) {
    return response instanceof ClientHttpResponse;
  }

  @Override
  public String extractStatusCode(Object response) {
    int status = 0;
    try {
      String statusHeaderKey = GovernanceHeaderStatusUtils.getStatusHeaderKey(environment);
      if (!StringUtils.isEmpty(((ClientHttpResponse) response).getHeaders().getFirst(statusHeaderKey))) {
        return ((ClientHttpResponse) response).getHeaders().getFirst(statusHeaderKey);
      }
      status = ((ClientHttpResponse) response).getStatusCode().value();
    } catch (IOException e) {
      LOGGER.error("unexpected exception", e);
    }
    return String.valueOf(status);
  }
}
