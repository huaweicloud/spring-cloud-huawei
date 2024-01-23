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
package com.huaweicloud.common.configration.dynamic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ContextProperties {
  public static final String PREFIX = "spring.cloud.servicecomb";

  public static final String REST_TEMPLATE_CONTEXT_ENABLED = PREFIX + "." + "restTemplate.context.enabled";

  public static final String FEIGN_CONTEXT_ENABLED = PREFIX + "." + "feign.context.enabled";

  public static final String WEBCLIENT_CONTEXT_ENABLED = PREFIX + "." + "webclient.context.enabled";

  public static final String GATEWAY_CONTEXT_ENABLED = PREFIX + "." + "gateway.context.enabled";

  private boolean enableTraceInfo = true;

  private boolean enableAsyncTrace = false;

  private String traceLevel;

  private List<String> traceContexts;

  private int waitTimeForShutDownInMillis = 3000;

  private Map<String, String> headerContextMapper = Collections.emptyMap();

  private Map<String, String> queryContextMapper = Collections.emptyMap();

  private boolean useContextOperationForMetrics = false;

  private boolean addTraceIdForFeign = false;

  private boolean addTraceIdForTemplate = false;

  public boolean isEnableTraceInfo() {
    return enableTraceInfo;
  }

  public void setEnableTraceInfo(boolean enableTraceInfo) {
    this.enableTraceInfo = enableTraceInfo;
  }

  public boolean isEnableAsyncTrace() {
    return enableAsyncTrace;
  }

  public void setEnableAsyncTrace(boolean enableAsyncTrace) {
    this.enableAsyncTrace = enableAsyncTrace;
  }

  public int getWaitTimeForShutDownInMillis() {
    return waitTimeForShutDownInMillis;
  }

  public void setWaitTimeForShutDownInMillis(int waitTimeForShutDownInMillis) {
    this.waitTimeForShutDownInMillis = waitTimeForShutDownInMillis;
  }

  public Map<String, String> getHeaderContextMapper() {
    return headerContextMapper;
  }

  public void setHeaderContextMapper(Map<String, String> headerContextMapper) {
    this.headerContextMapper = headerContextMapper;
  }

  public Map<String, String> getQueryContextMapper() {
    return queryContextMapper;
  }

  public void setQueryContextMapper(Map<String, String> queryContextMapper) {
    this.queryContextMapper = queryContextMapper;
  }

  public String getTraceLevel() {
    return traceLevel;
  }

  public void setTraceLevel(String traceLevel) {
    this.traceLevel = traceLevel;
  }

  public List<String> getTraceContexts() {
    return traceContexts;
  }

  public void setTraceContexts(List<String> traceContexts) {
    this.traceContexts = traceContexts;
  }

  public boolean isUseContextOperationForMetrics() {
    return useContextOperationForMetrics;
  }

  public void setUseContextOperationForMetrics(boolean useContextOperationForMetrics) {
    this.useContextOperationForMetrics = useContextOperationForMetrics;
  }

  public boolean isAddTraceIdForFeign() {
    return addTraceIdForFeign;
  }

  public void setAddTraceIdForFeign(boolean addTraceIdForFeign) {
    this.addTraceIdForFeign = addTraceIdForFeign;
  }

  public boolean isAddTraceIdForTemplate() {
    return addTraceIdForTemplate;
  }

  public void setAddTraceIdForTemplate(boolean addTraceIdForTemplate) {
    this.addTraceIdForTemplate = addTraceIdForTemplate;
  }
}
