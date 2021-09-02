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

package com.huaweicloud.chaincontext.mvc;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import com.huaweicloud.chaincontext.ChainContextProperties;
import com.huaweicloud.chaincontext.ChainContextHolder;
import com.huaweicloud.chaincontext.parser.IRequestKeyParser;
import com.huaweicloud.chaincontext.tracing.BraveTraceIdGenerator;

public class ChainContexthandlerInterceptor implements HandlerInterceptor {

  private ChainContextProperties chainContextProperties;

  private IRequestKeyParser requestKeyParser;

  public ChainContexthandlerInterceptor(ChainContextProperties chainContextProperties,
      IRequestKeyParser requestKeyParser) {
    this.chainContextProperties = chainContextProperties;
    this.requestKeyParser = requestKeyParser;
  }

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (CollectionUtils.isEmpty(chainContextProperties.getKeys())
        && StringUtils.isEmpty(chainContextProperties.getKeyPrefix())) {
      // No chain context rule config
      return true;
    } else if (CollectionUtils.isEmpty(chainContextProperties.getKeys())) {
      // All keys start with keyprefix will set to chain context
      Enumeration<String> headerEnumeration = request.getHeaderNames();
      while (headerEnumeration.hasMoreElements()) {
        String header = headerEnumeration.nextElement().toUpperCase();
        if (header.startsWith(chainContextProperties.getKeyPrefix())) {
          String value = requestKeyParser.parse(header, request);
          if (value != null) {
            // Add key-value to current context
            ChainContextHolder.getCurrentContext().put(header, value);
          } else {
            ChainContextHolder.getCurrentContext().put(BraveTraceIdGenerator.getTraceIdKeyName(),
                BraveTraceIdGenerator.generate());
          }
        }
      }
    } else {
      chainContextProperties.getKeys().forEach(key -> {
        String header = chainContextProperties.getKeyPrefix() + key;
        String value = requestKeyParser.parse(header, request);
        if (value != null) {
          // Add key-value to current context
          ChainContextHolder.getCurrentContext().put(header, value);
        }
      });
    }
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
      throws Exception {
    if (ChainContextHolder.getCurrentContext() != null) {
      ChainContextHolder.getCurrentContext().unset();
    }
  }
}
