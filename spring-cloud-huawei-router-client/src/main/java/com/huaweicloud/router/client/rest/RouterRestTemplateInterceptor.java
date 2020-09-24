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
package com.huaweicloud.router.client.rest;

import com.huaweicloud.router.client.header.HeaderPassUtil;
import java.io.IOException;

import com.huaweicloud.router.client.track.RouterTrackContext;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @Author GuoYl123
 * @Date 2019/10/11
 **/
public class RouterRestTemplateInterceptor implements ClientHttpRequestInterceptor {

  private static final String ROUTER_HEADER = "X-RouterContext";

  /**
   * header pass
   *
   * @param httpRequest
   * @param bytes
   * @param clientHttpRequestExecution
   * @return
   * @throws IOException
   */
  @Override
  public ClientHttpResponse intercept(HttpRequest httpRequest, byte[] bytes,
      ClientHttpRequestExecution clientHttpRequestExecution) throws IOException {
    RouterTrackContext.setServiceName(httpRequest.getURI().getHost());
    HttpHeaders headers = httpRequest.getHeaders();
    headers.add(ROUTER_HEADER, HeaderPassUtil.getPassHeaderString(headers.toSingleValueMap()));
    return clientHttpRequestExecution.execute(httpRequest, bytes);
  }
}
