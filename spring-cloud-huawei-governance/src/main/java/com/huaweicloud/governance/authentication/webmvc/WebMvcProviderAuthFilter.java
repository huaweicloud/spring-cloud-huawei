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

package com.huaweicloud.governance.authentication.webmvc;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;

import com.huaweicloud.governance.adapters.webmvc.WebMvcUtils;
import com.huaweicloud.governance.authentication.AccessController;
import com.huaweicloud.governance.authentication.AuthenticationAdapter;
import com.huaweicloud.governance.authentication.UnAuthorizedException;

public class WebMvcProviderAuthFilter implements Filter {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebMvcProviderAuthFilter.class);

  private final WebMvcRSAProviderAuthManager webMvcRSAProviderAuthManager;

  public WebMvcProviderAuthFilter(List<AccessController> accessControllers, Environment environment,
      AuthenticationAdapter authenticationAdapter) {
    webMvcRSAProviderAuthManager = new WebMvcRSAProviderAuthManager(accessControllers, environment, authenticationAdapter);
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
      throws IOException, ServletException {
    if (WebMvcUtils.isNotHttpServlet(request, response)) {
      chain.doFilter(request, response);
      return;
    }
    try {
      if (webMvcRSAProviderAuthManager.isRequiredAuth(((HttpServletRequest) request).getRequestURI())) {
        webMvcRSAProviderAuthManager.valid((HttpServletRequest) request);
      }
      chain.doFilter(request, response);
    } catch (Exception e) {
      if (e instanceof UnAuthorizedException) {
        ((HttpServletResponse) response).setStatus(403);
        response.getWriter().print(e.getMessage());
        LOGGER.warn("authentication failed: {}", e.getMessage());
      } else {
        throw new RuntimeException(e);
      }
    }
  }
}
