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
package com.huaweicloud.governance.adapters.feign;

import java.net.URI;
import java.util.Collection;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;

import feign.Request;

public class FeignUtils {
  private FeignUtils() {

  }

  public static GovernanceRequestExtractor convert(Request request, URI uri, String instanceId) {
    return new GovernanceRequestExtractor() {
      @Override
      public String apiPath() {
        return uri.getPath();
      }

      @Override
      public String method() {
        return request.httpMethod().name();
      }

      @Override
      public String header(String key) {
        Collection<String> headerValue = request.headers().get(key);
        if (headerValue != null && !headerValue.isEmpty()) {
          return headerValue.iterator().next();
        }
        return null;
      }

      @Override
      public String instanceId() {
        return instanceId;
      }

      @Override
      public String serviceName() {
        return uri.getHost();
      }

      @Override
      public Object sourceRequest() {
        return request;
      }
    };
  }
}
