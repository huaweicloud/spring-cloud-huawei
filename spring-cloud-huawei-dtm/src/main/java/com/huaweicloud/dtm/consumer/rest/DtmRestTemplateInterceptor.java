/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.huaweicloud.dtm.consumer.rest;

import java.io.IOException;
import java.util.Map;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huaweicloud.dtm.DtmConstants;

/**
 * @Author wangqijun
 * @Date 10:08 2019-09-18
 **/
public class DtmRestTemplateInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DtmRestTemplateInterceptor.class);

  @Override
  public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
      ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
    exportDtmContextToHeader(httpRequest);
    return clientHttpRequestExecution.execute(httpRequest, bytes);
  }

  private void exportDtmContextToHeader(HttpRequest httpRequest) {
    if (DtmConstants.DTM_CONTEXT_EX_METHOD == null) {
      return;
    }
    try {
      Object context = DtmConstants.DTM_CONTEXT_EX_METHOD.invoke(null);
      if (context instanceof Map) {
        httpRequest.getHeaders().add(DtmConstants.DTM_CONTEXT, JsonUtils.OBJ_MAPPER.writeValueAsString(context));
      }
    } catch (Throwable e) {
      // ignore
      LOGGER.warn("Failed to export dtm context", e);
    }
  }
}
