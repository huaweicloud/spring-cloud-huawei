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

package com.huaweicloud.service.engine.common.configration.dynamic;

public class DashboardProperties {
  private static final int MIN_INTERVAL = 5000;

  private String address;

  private boolean governanceProviderEnabled = false;

  private boolean invocationProviderEnabled = true;

  private int intervalInMills = 10000;

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    this.address = address;
  }

  public int getIntervalInMills() {
    if (intervalInMills <= MIN_INTERVAL) {
      return MIN_INTERVAL;
    }
    return intervalInMills;
  }

  public void setIntervalInMills(int intervalInMills) {
    this.intervalInMills = intervalInMills;
  }

  public boolean isGovernanceProviderEnabled() {
    return governanceProviderEnabled;
  }

  public void setGovernanceProviderEnabled(boolean governanceProviderEnabled) {
    this.governanceProviderEnabled = governanceProviderEnabled;
  }

  public boolean isInvocationProviderEnabled() {
    return invocationProviderEnabled;
  }

  public void setInvocationProviderEnabled(boolean invocationProviderEnabled) {
    this.invocationProviderEnabled = invocationProviderEnabled;
  }
}
