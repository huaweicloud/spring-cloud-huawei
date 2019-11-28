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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.huaweicloud.dtm.DtmContextDTO;
import com.huaweicloud.dtm.util.DtmConstants;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import com.huawei.middleware.dtm.client.context.DTMContext;

import io.vertx.core.json.Json;

/**
 * @Author wangqijun
 * @Date 10:08 2019-09-18
 **/
public class DtmRestTemplateInterceptor implements ClientHttpRequestInterceptor {

  private static final Logger LOGGER = LoggerFactory.getLogger(DtmRestTemplateInterceptor.class);

  @Override
  public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
      ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
    DTMContext dtmContext = DTMContext.getDTMContext();
    long gid = dtmContext.getGlobalTxId();
    HttpHeaders headers = httpRequest.getHeaders();
    if (gid != -1) {
      DtmContextDTO dtmContextDTO = DtmContextDTO.fromDtmContext(dtmContext);
      headers.add(DtmConstants.DTM_CONTEXT, Json.encode(dtmContextDTO));
    }
    return clientHttpRequestExecution.execute(httpRequest, bytes);
  }
}
