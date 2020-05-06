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
package com.huaweicloud.governance.service;

import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;

import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.policy.RateLimitingPolicy;
import com.huaweicloud.governance.properties.RateLimitProperties;

import java.util.List;

public class PolicyServiceImpl implements PolicyService {

  @Autowired
  private RateLimitProperties rateLimitProperties;

  @Override
  public List<Policy> getAllPolicies(String mark) {
    List<Policy> policies = new ArrayList<>();
    Policy ratePolicy = this.getRateLimitPolicy(mark);
    if (ratePolicy != null) {
      policies.add(ratePolicy);
    }
    return policies;
  }

  @Override
  public Policy getRateLimitPolicy(String mark) {
    RateLimitingPolicy policyResult;
    Map<String, RateLimitingPolicy> ratePolicies = rateLimitProperties.covert();
    if (mark == null) {
      policyResult = ratePolicies.get("global");
      if (policyResult != null) {
        policyResult.setName("global");
      }
      return policyResult;
    }
    for (Entry<String, RateLimitingPolicy> entry : ratePolicies.entrySet()) {
      if (entry.getValue().match(mark)) {
        policyResult = entry.getValue();
        policyResult.setName(entry.getKey());
        return policyResult;
      }
    }
    return null;
  }

  @Override
  public Policy getCircuitBreakerPolicy(String mark) {
    return null;
  }
}
