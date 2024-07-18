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

package com.huaweicloud.common.context;

import java.util.HashMap;
import java.util.Map;

import com.netflix.loadbalancer.Server;

public class InvocationContext {
  private static final ThreadLocal<Map<String, Object>> CONTEXT = new ThreadLocal<>();

  public static final String CONTEXT_CURRENT_INSTANCE = "x-current-instance";

  public static void setCurrentInstanse(Server server) {
    Map<String, Object> map = new HashMap<>();
    map.put(CONTEXT_CURRENT_INSTANCE, server);
    CONTEXT.set(map);
  }

  public static Server getCurrentInstanse() {
    if (CONTEXT.get() != null) {
      return (Server) CONTEXT.get().get(CONTEXT_CURRENT_INSTANCE);
    }
    return null;
  }
}
