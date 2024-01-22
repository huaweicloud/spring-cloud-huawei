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

public class LoadBalancerProperties {
  public static final String LOADBALANCER_ENABLED = "spring.cloud.servicecomb.loadbalancer.enabled";

  public static final String RULE_ROUND_ROBIN = "RoundRobin";

  public static final String RULE_RANDOM = "Random";

  private String rule = RULE_ROUND_ROBIN;

  public String getRule() {
    return rule;
  }

  public void setRule(String rule) {
    this.rule = rule;
  }
}
