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

package com.huaweicloud.common.adapters.web;

import java.io.IOException;

import org.springframework.core.Ordered;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;
import com.huaweicloud.common.context.InvocationStage;

public class MetricsClientHttpRequestInterceptor implements
    ClientHttpRequestInterceptor, Ordered {

  public MetricsClientHttpRequestInterceptor() {

  }

  @Override
  public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution)
      throws IOException {
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    String stageName = context.getInvocationStage().recordStageBegin(stageId(request));
    try {
      ClientHttpResponse response = execution.execute(request, body);
      context.getInvocationStage().recordStageEnd(stageName);
      return response;
    } catch (Throwable error) {
      context.getInvocationStage().recordStageEnd(stageName);
      throw error;
    }
  }

  private String stageId(HttpRequest request) {
    return InvocationStage.STAGE_REST_TEMPLATE + " " + request.getMethod().name() + " " + request.getURI().getPath();
  }

  @Override
  public int getOrder() {
    // after RestTemplateAddServiceNameContext
    return Ordered.HIGHEST_PRECEDENCE + 2;
  }
}
