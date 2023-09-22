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

package com.huaweicloud.governance.authentication;

import java.util.Map;

import org.apache.servicecomb.governance.marker.GovernanceRequestExtractor;
import org.springframework.util.LinkedCaseInsensitiveMap;

public class AuthRequestExtractor implements GovernanceRequestExtractor {
  private Map<String, String> headers;

  private String apiPath;

  private String method;

  private String instanceId;

  private String serviceName;

  private String serviceId;

  private Object sourceRequest;

  private String token;

  @Override
  public String apiPath() {
    return apiPath;
  }

  public void setApiPath(String apiPath) {
    this.apiPath = apiPath;
  }

  @Override
  public String method() {
    return method;
  }

  public void setMethod(String method) {
    this.method = method;
  }

  @Override
  public String header(String key) {
    return headers.get(key);
  }

  public Map<String, String> getHeaders() {
    return headers;
  }

  public void setHeaders(Map<String, String> headers) {
    Map<String, String> temp = new LinkedCaseInsensitiveMap<>();
    temp.putAll(headers);
    this.headers = temp;
  }

  @Override
  public String instanceId() {
    return instanceId;
  }

  public void setInstanceId(String instanceId) {
    this.instanceId = instanceId;
  }

  @Override
  public String serviceName() {
    return serviceName;
  }

  public void setServiceName(String serviceName) {
    this.serviceName = serviceName;
  }

  public String serviceId() {
    return serviceId;
  }

  public void setServiceId(String serviceId) {
    this.serviceId = serviceId;
  }

  @Override
  public Object sourceRequest() {
    return sourceRequest;
  }

  public void setSourceRequest(Object sourceRequest) {
    this.sourceRequest = sourceRequest;
  }

  public String token() {
    return token;
  }

  public void setToken(String token) {
    this.token = token;
  }
}
