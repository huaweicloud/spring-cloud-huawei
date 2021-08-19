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
package com.huaweicloud.router.client.track;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.huaweicloud.common.util.HeaderUtil;

/**
 * 将服务端收到的HTTP请求头设置到线程上下文中， 供Client发送请求的时候使用。
 **/
public class RouterHandlerInterceptor implements HandlerInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(RouterHandlerInterceptor.class);

  @Autowired(required = false)
  private List<RouterHeaderFilterExt> filters;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    if (request.getHeader(RouterTrackContext.ROUTER_TRACK_HEADER) != null) {
      RouterTrackContext.setRequestHeader(request.getHeader(RouterTrackContext.ROUTER_TRACK_HEADER));
      return true;
    }

    Map<String, String> headers = HeaderUtil.getHeaders(request);
    if (!CollectionUtils.isEmpty(filters)) {
      for (RouterHeaderFilterExt filterExt : filters) {
        if (filterExt.enabled()) {
          headers = filterExt.doFilter(headers);
        }
      }
    }
    try {
      RouterTrackContext.setRequestHeader(JsonUtils.writeValueAsString(headers));
    } catch (JsonProcessingException e) {
      LOGGER.warn("encode headers failed for {}", e.getMessage());
    }

    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, @Nullable Exception ex) {
    RouterTrackContext.remove();
  }
}
