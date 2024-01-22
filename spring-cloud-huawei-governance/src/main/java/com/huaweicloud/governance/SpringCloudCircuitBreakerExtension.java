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
package com.huaweicloud.governance;

import java.util.List;

import org.apache.servicecomb.governance.handler.ext.AbstractCircuitBreakerExtension;

public class SpringCloudCircuitBreakerExtension extends AbstractCircuitBreakerExtension {
  private final SpringCloudRetryExtension retryExtension;

  private final List<StatusCodeExtractor> statusCodeExtractors;

  public SpringCloudCircuitBreakerExtension(List<StatusCodeExtractor> statusCodeExtractors) {
    this.statusCodeExtractors = statusCodeExtractors;
    this.retryExtension = new SpringCloudRetryExtension(this.statusCodeExtractors);
  }

  @Override
  protected String extractStatusCode(Object response) {
    return this.retryExtension.extractStatusCode(response);
  }

  @Override
  public boolean isFailedResult(Throwable e) {
    if (isRuntimeException(e)) {
      return true;
    }
    return super.isFailedResult(e);
  }

  private boolean isRuntimeException(Throwable e) {
    if (e == null) {
      return false;
    }
    if (e instanceof RuntimeException) {
      return true;
    }
    return isRuntimeException(e.getCause());
  }
}
