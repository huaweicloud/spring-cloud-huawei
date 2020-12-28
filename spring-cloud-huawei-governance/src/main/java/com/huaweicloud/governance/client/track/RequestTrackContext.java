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
package com.huaweicloud.governance.client.track;

import java.util.List;

import org.apache.servicecomb.governance.policy.Policy;

public class RequestTrackContext {

  private static ThreadLocal<ServerExcluder> serverThreadLocal = new ThreadLocal<>();

  private static ThreadLocal<List<Policy>> policyThreadLocal = new ThreadLocal<>();

  public static void remove() {
    serverThreadLocal.remove();
    policyThreadLocal.remove();
  }

  public static List<Policy> getPolicies() {
    return policyThreadLocal.get();
  }

  public static void setPolicies(List<Policy> policies) {
    policyThreadLocal.set(policies);
  }

  public static ServerExcluder getServerExcluder() {
    if (serverThreadLocal.get() == null) {
      serverThreadLocal.set(new ServerExcluder());
    }
    return serverThreadLocal.get();
  }
}
