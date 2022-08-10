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

import org.springframework.core.Ordered;

public class GovernanceProperties {
  public static final String PREFIX = "spring.cloud.servicecomb";

  public static final String GATEWAY_GOVERNANCE_ENABLED = PREFIX + "." + "gateway.governance.enabled";

  public static final String GATEWAY_RATE_LIMITING_ENABLED = PREFIX + "." + "gateway.rateLimiting.enabled";

  public static final String GATEWAY_FAULT_INJECTION_ENABLED = PREFIX + "." + "gateway.faultInjection.enabled";

  public static final String GATEWAY_INSTANCE_ISOLATION_ENABLED = PREFIX + "." + "gateway.instanceIsolation.enabled";

  public static class Gateway {
    private RateLimiting rateLimiting = new RateLimiting();

    public RateLimiting getRateLimiting() {
      return rateLimiting;
    }

    public void setRateLimiting(RateLimiting rateLimiting) {
      this.rateLimiting = rateLimiting;
    }
  }

  public static class RateLimiting {
    private int order = Ordered.HIGHEST_PRECEDENCE;

    public int getOrder() {
      return order;
    }

    public void setOrder(int order) {
      this.order = order;
    }
  }

  private Gateway gateway = new Gateway();

  public Gateway getGateway() {
    return gateway;
  }

  public void setGateway(Gateway gateway) {
    this.gateway = gateway;
  }
}
