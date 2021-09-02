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

package com.huaweicloud.chaincontext.feign;

import com.huaweicloud.chaincontext.ChainContextHolder;
import com.huaweicloud.chaincontext.tracing.BraveTraceIdGenerator;

import feign.RequestInterceptor;
import feign.RequestTemplate;

public class ChainContextRequestInterceptor implements RequestInterceptor {

  @Override
  public void apply(RequestTemplate template) {
    if (ChainContextHolder.getCurrentContext() != null) {
      ChainContextHolder.getCurrentContext().forEach((key, value) -> {
        template.header(key, value.toString());
      });
    } else {
      String key = BraveTraceIdGenerator.getTraceIdKeyName();
      String value = BraveTraceIdGenerator.generate();
      ChainContextHolder.getCurrentContext().put(key, value);
      template.header(key, value);
    }
  }
}
