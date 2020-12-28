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
package org.apache.servicecomb.governance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.servicecomb.governance.policy.AbstractPolicy;
import org.apache.servicecomb.governance.policy.Policy;
import org.apache.servicecomb.governance.properties.BulkheadProperties;
import org.apache.servicecomb.governance.properties.CircuitBreakerProperties;
import org.apache.servicecomb.governance.properties.RateLimitProperties;
import org.apache.servicecomb.governance.properties.RetryProperties;
import org.apache.servicecomb.governance.service.MatchHashModel;
import org.apache.servicecomb.governance.service.MatchersService;
import org.apache.servicecomb.governance.service.PolicyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import org.apache.servicecomb.governance.marker.GovHttpRequest;

@Component
public class MatchersManager {

  @Autowired
  private MatchersService matchersService;

  @Autowired
  private PolicyService policyService;

  public MatchersManager() {
  }

  public Map<String, Policy> match(GovHttpRequest request) {
    /**
     * 1.获取该请求携带的marker
     */
    List<String> marks = matchersService.getMatchedNames(request);
    /**
     * 2.通过 marker获取到所有的policy
     */
    return policyService.getAllPolicies(marks);
  }


  /**
   * 使用于match较多的情况，不对match进行全量的匹配，而是根据policy按需匹配
   *
   * @param request
   * @return
   */
  public Map<String, Policy> matchByPolicy(GovHttpRequest request) {
    Map<String, Policy> result = new HashMap<>();
    String[] policyNames = {
        RateLimitProperties.class.getName(),
        RetryProperties.class.getName(),
        CircuitBreakerProperties.class.getName(),
        BulkheadProperties.class.getName()
    };
    MatchHashModel match = matchersService.getMatchHashModel(request);
    for (String policyName : policyNames) {
      Policy policy = getPolicyByKind(policyName, match);
      if (policy != null) {
        result.put(policyName, policy);
      }
    }
    return result;
  }


  private Policy getPolicyByKind(String kind, MatchHashModel match) {
    List<AbstractPolicy> policyList = new ArrayList<>();
    for (Entry<String, Policy> entry : policyService.getCustomPolicy(kind).entrySet()) {
      AbstractPolicy policy = (AbstractPolicy) entry.getValue();
      if (matchersService.process(entry.getKey(), policy, match)) {
        policyList.add((AbstractPolicy) entry.getValue());
      }
    }
    if (policyList.isEmpty()) {
      return null;
    }
    policyList.sort(Comparator.comparingInt(p -> p.getRules().getPrecedence()));
    return policyList.get(0);
  }
}
