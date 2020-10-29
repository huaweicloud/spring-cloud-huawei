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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.huaweicloud.common.util.HeaderUtil;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class RouterHandlerInterceptor implements HandlerInterceptor {

  @Autowired(required = false)
  private List<RouterHeaderFilterExt> filters;

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    Map<String, String> headers = HeaderUtil.getHeaders(request);
    if (!CollectionUtils.isEmpty(filters)) {
      for (RouterHeaderFilterExt filterExt : filters) {
        if (filterExt.enabled()) {
          headers = filterExt.doFilter(headers);
        }
      }
    }
    RouterTrackContext.setRequestHeader(headers);
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, @Nullable Exception ex) {
    RouterTrackContext.remove();
  }
}
