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

package com.huaweicloud.governance.adapters.loadbalancer;

import java.util.List;

import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequest;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancerRequestTransformer;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;

public class DecorateLoadBalancerRequestFactory extends LoadBalancerRequestFactory {

  public DecorateLoadBalancerRequestFactory(LoadBalancerClient loadBalancer,
      List<LoadBalancerRequestTransformer> transformers) {
    super(loadBalancer, transformers);
  }

  @Override
  public LoadBalancerRequest<ClientHttpResponse> createRequest(final HttpRequest request, final byte[] body,
      final ClientHttpRequestExecution execution) {
    LoadBalancerRequest<ClientHttpResponse> loadBalancerRequest = super.createRequest(request, body, execution);
    DecorateLoadBalancerRequest routerLoadBalancerRequest = new DecorateLoadBalancerRequest(loadBalancerRequest);
    routerLoadBalancerRequest.setRequest(request);
    return routerLoadBalancerRequest;
  }
}
