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
package org.apache.servicecomb.governance.marker;

import java.util.HashMap;
import java.util.Map;

public class GovernanceRequest {
  private Map<String, String> headers;

  private String uri;

  private String method;

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    this.headers = transUpperKey2Low(headers);
  }

  public String getUri() {
    return uri;
  }

  public void setUri(String uri) {
    this.uri = uri;
  }

  public String getMethod() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  private Map<String, String> transUpperKey2Low(Map<String, String> headers) {
    Map<String, String> result = new HashMap<>();
    if (headers == null || headers.isEmpty()) {
      return result;
    }
    for (String key : headers.keySet()) {
      result.put(key.toLowerCase(), headers.get(key));
    }
    return result;
  }
}
