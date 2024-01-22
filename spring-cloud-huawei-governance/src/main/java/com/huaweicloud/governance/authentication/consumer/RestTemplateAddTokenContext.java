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

package com.huaweicloud.governance.authentication.consumer;

import com.huaweicloud.common.adapters.web.PreClientHttpRequestInterceptor;
import com.huaweicloud.common.context.InvocationContextHolder;
import org.springframework.http.HttpRequest;

public class RestTemplateAddTokenContext implements PreClientHttpRequestInterceptor {

  private final RSAConsumerTokenManager authenticationTokenManager;

  public RestTemplateAddTokenContext(RSAConsumerTokenManager authenticationTokenManager) {
    this.authenticationTokenManager = authenticationTokenManager;
  }

  @Override
  public void process(HttpRequest request, byte[] body) {
    authenticationTokenManager.setToken(InvocationContextHolder.getOrCreateInvocationContext());
  }
}
