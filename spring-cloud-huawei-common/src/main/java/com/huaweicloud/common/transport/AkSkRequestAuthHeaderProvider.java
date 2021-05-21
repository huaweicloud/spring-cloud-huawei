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

package com.huaweicloud.common.transport;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.foundation.auth.AuthHeaderProvider;
import org.apache.servicecomb.http.client.common.HttpUtils;

public class AkSkRequestAuthHeaderProvider implements AuthHeaderProvider {
  public static final String X_SERVICE_AK = "X-Service-AK";

  public static final String X_SERVICE_SHA_AKSK = "X-Service-ShaAKSK";

  public static final String X_SERVICE_PROJECT = "X-Service-Project";

  private ServiceCombAkSkProperties serviceCombAkSkProperties;

  public AkSkRequestAuthHeaderProvider(ServiceCombAkSkProperties serviceCombAkSkProperties) {
    this.serviceCombAkSkProperties = serviceCombAkSkProperties;
  }

  @Override
  public Map<String, String> authHeaders() {
    if (isAKSKNotEnabled(serviceCombAkSkProperties)) {
      return Collections.emptyMap();
    }
    Map<String, String> headers = new HashMap<>();
    headers.put(X_SERVICE_AK, serviceCombAkSkProperties.getAccessKey());
    headers.put(X_SERVICE_SHA_AKSK, serviceCombAkSkProperties.getSecretKey());
    headers.put(X_SERVICE_PROJECT, encode(serviceCombAkSkProperties.getProject()));
    return headers;
  }

  private String encode(String content) {
    try {
      return HttpUtils.encodeURLParam(content);
    } catch (IOException e) {
      return content;
    }
  }

  private static boolean isAKSKNotEnabled(ServiceCombAkSkProperties serviceCombAkSkProperties) {
    return serviceCombAkSkProperties == null ||
        serviceCombAkSkProperties.isEmpty() ||
        !serviceCombAkSkProperties.isEnabled();
  }
}
