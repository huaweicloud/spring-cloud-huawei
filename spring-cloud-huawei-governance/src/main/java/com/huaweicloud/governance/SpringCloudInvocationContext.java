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

package com.huaweicloud.governance;

import java.util.HashMap;
import java.util.Map;

import org.apache.servicecomb.governance.InvocationContext;

public class SpringCloudInvocationContext implements InvocationContext {
  private static ThreadLocal<Map<String, Boolean>> context = new ThreadLocal<>();

  public static void setInvocationContext() {
    context.set(new HashMap<>());
  }

  public static void removeInvocationContext() {
    context.remove();
  }

  @Override
  public Map<String, Boolean> getCalculatedMatches() {
    return context.get();
  }

  @Override
  public void addMatch(String key, Boolean value) {
    Map<String, Boolean> result = context.get();
    result.put(key, value);
  }
}
