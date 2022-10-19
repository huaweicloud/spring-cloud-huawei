/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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
package com.huaweicloud.sample;

import org.apache.http.client.HttpClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.loadbalancer.support.LoadBalancerClientFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.loadbalancer.FeignBlockingLoadBalancerClient;
import org.springframework.context.annotation.Bean;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import feign.Client;
import feign.httpclient.ApacheHttpClient;

// define a customized Client for it so that absolute url can use the default spring cloud client.
// see: https://github.com/huaweicloud/spring-cloud-huawei/issues/923
// TODO: in future may have better solutions when spring cloud open feign add support
// see: https://github.com/huaweicloud/spring-cloud-huawei/issues/923
@FeignClient(name = "urlPrice", url = "http://127.0.0.1:9090",
    configuration = UrlFeignService.Configuration.class)
public interface UrlFeignService {
  @PostMapping("/price")
  String getPrice(@RequestParam("id") Long id);

  class Configuration {
    @Bean
    public Client urlFeignServiceClient(LoadBalancerClient loadBalancerClient, HttpClient httpClient,
        LoadBalancerClientFactory loadBalancerClientFactory) {
      ApacheHttpClient delegate = new ApacheHttpClient(httpClient);
      return new FeignBlockingLoadBalancerClient(delegate, loadBalancerClient, loadBalancerClientFactory);
    }
  }
}
