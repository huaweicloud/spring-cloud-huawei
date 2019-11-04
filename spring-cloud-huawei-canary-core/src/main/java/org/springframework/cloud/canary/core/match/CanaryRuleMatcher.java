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
package org.springframework.cloud.canary.core.match;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.canary.core.cache.CanaryRuleCache;
import org.springframework.cloud.canary.core.model.PolicyRuleItem;

/**
 * @Author GuoYl123
 * @Date 2019/10/17
 **/
public class CanaryRuleMatcher {

  @Autowired(required = false)
  private CanaryHeaderFilterExt canaryHeaderFilterExt;

  private static CanaryRuleMatcher instance = new CanaryRuleMatcher();

  private CanaryRuleMatcher() {
  }

  /**
   * 匹配到合适的rule 匹配规则即： source (目标服务名字) sourceTags （一期先不考虑） headers （匹配header字段）
   *
   * @param serviceName
   * @return
   */
  public PolicyRuleItem match(String serviceName, Map<String, String> invokeHeader) {
    if (canaryHeaderFilterExt != null) {
      invokeHeader = canaryHeaderFilterExt.doFilter(invokeHeader);
    }
    for (PolicyRuleItem rule : CanaryRuleCache.getServiceInfoCacheMap().get(serviceName)
        .getAllrule()) {
      if (rule.getMatch() == null || rule.getMatch().match(invokeHeader)) {
        return rule;
      }
    }
    return null;
  }

  public static CanaryRuleMatcher getInstance() {
    return instance;
  }
}
