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

import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.loadbalancer.reactive.LoadBalancerClientRequestTransformer;
import org.springframework.web.reactive.function.client.ClientRequest;

public class ContextLoadBalancerClientRequestTransformer implements LoadBalancerClientRequestTransformer {
  @Override
  public ClientRequest transformRequest(ClientRequest request, ServiceInstance instance) {
    return ClientRequest.from(request)
        .attribute(RetryContext.RETRY_SERVICE_INSTANCE, instance)
        .build();
  }
}
