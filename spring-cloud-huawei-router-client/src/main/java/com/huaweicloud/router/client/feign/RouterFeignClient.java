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
package com.huaweicloud.router.client.feign;

import java.io.IOException;
import java.net.URI;

import com.huaweicloud.router.client.track.RouterTrackContext;

import feign.Client;
import feign.Request;
import feign.Response;

/**
 * @Author GuoYl123
 * @Date 2019/10/22
 **/
public class RouterFeignClient implements Client {

  private Client client;

  public RouterFeignClient(Client client) {
    this.client = client;
  }

  @Override
  public Response execute(Request request, Request.Options options) throws IOException {
    URI uri = URI.create(request.url());
    RouterTrackContext.setServiceName(uri.getHost());
    return client.execute(request, options);
  }
}
