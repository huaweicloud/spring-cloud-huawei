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

import com.huaweicloud.governance.policy.AbstractPolicy;
import com.huaweicloud.governance.policy.Policy;
import com.huaweicloud.governance.properties.GovProperties;

import java.util.List;

public class PolicyServiceImpl implements PolicyService {

  private static final String MATCH_NONE = "none";

  @Autowired
  private List<GovProperties> propertiesList;

  @Override
  public List<Policy> getAllPolicies(String mark) {
    List<Policy> policies = new ArrayList<>();
    for (GovProperties properties : propertiesList) {
      Policy ratePolicy = match(properties.covert(), mark);
      if (ratePolicy != null) {
        policies.add(ratePolicy);
      }
    }
    return policies;
  }

  @Override
  public Policy getCustomPolicy(String kind, String mark) {
    for (GovProperties properties : propertiesList) {
      if (properties.getClass().getName().startsWith(kind)) {
        return match(properties.covert(), mark);
      }
    }
    return null;
  }

  private <T extends AbstractPolicy> Policy match(Map<String, T> policies, String mark) {
    AbstractPolicy policyResult;
    if (mark == null) {
      mark = MATCH_NONE;
    }
    for (Entry<String, T> entry : policies.entrySet()) {
      if (entry.getValue().match(mark)) {
        policyResult = entry.getValue();
        policyResult.setName(entry.getKey());
        return policyResult;
      }
    }
    return null;
  }
}
