package com.huaweicloud.common.configration.dynamic;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;

@RefreshScope
@ConfigurationProperties("spring.cloud.servicecomb.dashboard")
public class DashboardProperties {
  private static final int MIN_INTERVAL = 5000;

  private String address;

  private boolean governanceProviderEnabled = true;

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
}
