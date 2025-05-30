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

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.springframework.util.CollectionUtils;

import com.huaweicloud.rocketmq.grayscale.holder.RequestGrayHeaderHolder;
import com.huaweicloud.rocketmq.grayscale.RocketMqMessageGrayUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public class WebMvcGrayHeaderFilter implements Filter {
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    HttpServletRequest servletRequest = (HttpServletRequest) request;
    Map<String, HashSet<String>> trafficTags = RocketMqMessageGrayUtils.getAllTrafficTagMap();
    Map<String, String> matchHeaders = new HashMap<>();
    for (String key : trafficTags.keySet()) {
      if (servletRequest.getHeader(key) != null && trafficTags.get(key).contains(servletRequest.getHeader(key))) {
        matchHeaders.put(key, servletRequest.getHeader(key));
      }
    }
    if (!CollectionUtils.isEmpty(matchHeaders)) {
      RequestGrayHeaderHolder.setRequestGrayHeader(matchHeaders);
    }
    chain.doFilter(request, response);
  }
}
