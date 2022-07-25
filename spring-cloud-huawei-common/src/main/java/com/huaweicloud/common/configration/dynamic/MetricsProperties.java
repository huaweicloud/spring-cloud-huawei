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

package com.huaweicloud.common.configration.dynamic;

public class MetricsProperties {
  private int maxMethodCount = 1000;

  private String includePattern;

  private String excludePattern;

  public int getMaxMethodCount() {
    return maxMethodCount;
  }

  public void setMaxMethodCount(int maxMethodCount) {
    this.maxMethodCount = maxMethodCount;
  }

  public String getIncludePattern() {
    return includePattern;
  }

  public void setIncludePattern(String includePattern) {
    this.includePattern = includePattern;
  }

  public String getExcludePattern() {
    return excludePattern;
  }

  public void setExcludePattern(String excludePattern) {
    this.excludePattern = excludePattern;
  }
}
