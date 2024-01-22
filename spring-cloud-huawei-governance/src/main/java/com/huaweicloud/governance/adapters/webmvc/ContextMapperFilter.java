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
package com.huaweicloud.governance.adapters.webmvc;

import java.io.IOException;
import java.util.Map;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.servicecomb.governance.handler.MapperHandler;
import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.apache.servicecomb.governance.processor.mapping.Mapper;
import org.springframework.util.CollectionUtils;

import com.huaweicloud.common.context.InvocationContext;
import com.huaweicloud.common.context.InvocationContextHolder;

public class ContextMapperFilter implements Filter {
  private final MapperHandler mapperHandler;

  public ContextMapperFilter(MapperHandler mapperHandler) {
    this.mapperHandler = mapperHandler;
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (!(request instanceof HttpServletRequest && response instanceof HttpServletResponse)) {
      chain.doFilter(request, response);
      return;
    }

    GovernanceRequestExtractor governanceRequest = WebMvcUtils.convert((HttpServletRequest) request);
    Mapper mapper = mapperHandler.getActuator(governanceRequest);
    if (mapper == null || CollectionUtils.isEmpty(mapper.target())) {
      chain.doFilter(request, response);
      return;
    }
    Map<String, String> properties = mapper.target();
    InvocationContext context = InvocationContextHolder.getOrCreateInvocationContext();
    properties.forEach((k, v) -> {
      if (StringUtils.isEmpty(v)) {
        return;
      }
      if ("$U".equals(v)) {
        context.putContext(k, governanceRequest.apiPath());
      } else if ("$M".equals(v)) {
        context.putContext(k, governanceRequest.method());
      } else if (v.startsWith("$H{") && v.endsWith("}")) {
        context.putContext(k, governanceRequest.header(v.substring(3, v.length() - 1)));
      } else {
        context.putContext(k, v);
      }
    });
    chain.doFilter(request, response);
  }
}
