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

public class InvocationContext {
  protected Map<String, String> context = new HashMap<>();

  protected Map<String, Object> localContext = new HashMap<>();

  public InvocationContext putContext(String key, String value) {
    context.put(key, value);
    return this;
  }

  public InvocationContext putContext(Map<String, String> data) {
    context.putAll(data);
    return this;
  }

  public String getContext(String key) {
    return context.get(key);
  }

  public Map<String, String> getContext() {
    return context;
  }

  public InvocationContext putLocalContext(String key, String value) {
    localContext.put(key, value);
    return this;
  }

  @SuppressWarnings("unchecked")
  public <T> T getLocalContext(String key) {
    return (T) localContext.get(key);
  }
}
