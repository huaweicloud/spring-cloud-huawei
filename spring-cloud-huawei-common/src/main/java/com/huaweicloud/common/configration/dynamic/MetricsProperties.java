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

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class MetricsProperties {
  private int maxMetricsCount = 2000;

  private String includePattern;

  private String excludePattern;

  private Duration distributionStatisticExpiry = Duration.ofMillis(Long.MAX_VALUE);

  private List<Integer> serviceLevelObjectives = Arrays.asList(10, 20, 50, 100, 500, 2000, 60000);

  private boolean logsEnabled = true;

  private Duration logsInterval = Duration.ofMinutes(15);

  public int getMaxMetricsCount() {
    return maxMetricsCount;
  }

  public void setMaxMetricsCount(int maxMetricsCount) {
    this.maxMetricsCount = maxMetricsCount;
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

  public Duration getDistributionStatisticExpiry() {
    return distributionStatisticExpiry;
  }

  public void setDistributionStatisticExpiry(Duration distributionStatisticExpiry) {
    this.distributionStatisticExpiry = distributionStatisticExpiry;
  }

  public List<Integer> getServiceLevelObjectives() {
    return serviceLevelObjectives;
  }

  public void setServiceLevelObjectives(List<Integer> serviceLevelObjectives) {
    this.serviceLevelObjectives = serviceLevelObjectives;
  }

  public boolean isLogsEnabled() {
    return logsEnabled;
  }

  public void setLogsEnabled(boolean logsEnabled) {
    this.logsEnabled = logsEnabled;
  }

  public Duration getLogsInterval() {
    return logsInterval;
  }

  public void setLogsInterval(Duration logsInterval) {
    this.logsInterval = logsInterval;
  }
}
