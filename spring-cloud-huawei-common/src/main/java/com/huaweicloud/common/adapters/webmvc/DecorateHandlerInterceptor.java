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

package com.huaweicloud.common.adapters.webmvc;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * add invocation context and metrics support for webmvc
 */
public class DecorateHandlerInterceptor implements HandlerInterceptor, Ordered {
  private final List<PreHandlerInterceptor> preHandlerInterceptors;

  private final List<PostHandlerInterceptor> postHandlerInterceptors;

  public DecorateHandlerInterceptor(List<PreHandlerInterceptor> preHandlerInterceptors,
      List<PostHandlerInterceptor> postHandlerInterceptors) {
    this.preHandlerInterceptors = preHandlerInterceptors;
    this.postHandlerInterceptors = postHandlerInterceptors;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (preHandlerInterceptors == null) {
      return true;
    }
    for (PreHandlerInterceptor preHandler : preHandlerInterceptors) {
      if (!preHandler.handle(request, response, handler)) {
        return false;
      }
    }
    return true;
  }

  @Override
  public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
      @Nullable ModelAndView modelAndView) throws Exception {
    if (postHandlerInterceptors == null) {
      return;
    }
    for (PostHandlerInterceptor postHandlerInterceptor : postHandlerInterceptors) {
      postHandlerInterceptor.handle(request, response, handler, modelAndView);
    }
  }

  @Override
  public int getOrder() {
    return 0;
  }
}
