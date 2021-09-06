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

package com.huaweicloud.router.client.loabalancer;

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpResponse;

public class RouterLoadBalancerRequest implements LoadBalancerRequest<ClientHttpResponse> {
  private LoadBalancerRequest<ClientHttpResponse> delegate;

  private HttpRequest request;

  public RouterLoadBalancerRequest(LoadBalancerRequest<ClientHttpResponse> delegate) {
    this.delegate = delegate;
  }

  public HttpRequest getRequest() {
    return this.request;
  }

  public void setRequest(HttpRequest request) {
    this.request = request;
  }

  @Override
  public ClientHttpResponse apply(ServiceInstance instance) throws Exception {
    return this.delegate.apply(instance);
  }
}
