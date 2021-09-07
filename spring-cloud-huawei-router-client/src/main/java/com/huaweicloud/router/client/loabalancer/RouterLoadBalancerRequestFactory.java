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

import java.util.List;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestTransformer;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

public class RouterLoadBalancerRequestFactory extends LoadBalancerRequestFactory {

  public RouterLoadBalancerRequestFactory(LoadBalancerClient loadBalancer,
                                          List<LoadBalancerRequestTransformer> transformers) {
    super(loadBalancer, transformers);
  }

  public RouterLoadBalancerRequestFactory(LoadBalancerClient loadBalancer) {
    super(loadBalancer);
  }

  @Override
  public LoadBalancerRequest<ClientHttpResponse> createRequest(final HttpRequest request, final byte[] body,
      final ClientHttpRequestExecution execution) {
    LoadBalancerRequest<ClientHttpResponse> loadBalancerRequest = super.createRequest(request, body, execution);
    RouterLoadBalancerRequest routerLoadBalancerRequest = new RouterLoadBalancerRequest(loadBalancerRequest);
    routerLoadBalancerRequest.setRequest(request);
    return routerLoadBalancerRequest;
  }
}
