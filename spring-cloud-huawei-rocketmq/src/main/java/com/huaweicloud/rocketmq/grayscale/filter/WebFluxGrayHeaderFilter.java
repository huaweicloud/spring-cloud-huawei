/*

 * Copyright (C) 2020-2025 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.rocketmq.grayscale.filter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.web.reactive.filter.OrderedWebFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilterChain;

import com.huaweicloud.rocketmq.grayscale.holder.RequestGrayHeaderHolder;
import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;

import reactor.core.publisher.Mono;

public class WebFluxGrayHeaderFilter implements OrderedWebFilter {
  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
    Map<String, HashSet<String>> trafficTags = RocketMqMessageGrayUtils.getAllTrafficTagMap();
    if (CollectionUtils.isEmpty(trafficTags) || exchange.getRequest().getHeaders().isEmpty()) {
      return chain.filter(exchange);
    }
    Map<String, String> matchHeaders = new HashMap<>();
    HttpHeaders requestHeaders = exchange.getRequest().getHeaders();
    for (String key : trafficTags.keySet()) {
      String headerValue = requestHeaders.getFirst(key);
      if (!StringUtils.isEmpty(headerValue) && trafficTags.get(key).contains(headerValue)) {
        matchHeaders.put(key, headerValue);
      }
    }
    if (!CollectionUtils.isEmpty(matchHeaders)) {
      RequestGrayHeaderHolder.setRequestGrayHeader(matchHeaders);
    }
    return chain.filter(exchange);
  }
}
