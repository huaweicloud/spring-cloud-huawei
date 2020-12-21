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
package com.huaweicloud.governance.policy;

import java.util.List;

public abstract class AbstractPolicy implements Policy {

  private String name;

  private GovRule rules;

  public GovRule getRules() {
    return rules;
  }

  public void setRules(GovRule rules) {
    this.rules = rules;
  }

  public void setName(String name) {
    this.name = name;
  }

  @Override
  public boolean match(List<String> strs) {
    return strs.stream().anyMatch(str -> rules.getMatch().contains(str));
  }

  @Override
  public String name() {
    return name;
  }
}
