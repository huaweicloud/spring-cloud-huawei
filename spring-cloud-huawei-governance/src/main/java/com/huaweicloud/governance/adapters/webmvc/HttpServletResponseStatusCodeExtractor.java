/*

 * Copyright (C) 2020-2022 Huawei Technologies Co., Ltd. All rights reserved.

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

import com.huaweicloud.governance.StatusCodeExtractor;

import jakarta.servlet.http.HttpServletResponse;

public class HttpServletResponseStatusCodeExtractor implements StatusCodeExtractor {
  @Override
  public boolean canProcess(Object response) {
    return response instanceof HttpServletResponse;
  }

  @Override
  public String extractStatusCode(Object response) {
    return String.valueOf(((HttpServletResponse) response).getStatus());
  }
}
