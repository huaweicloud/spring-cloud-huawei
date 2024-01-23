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

package com.huaweicloud.common.context;

import com.huaweicloud.common.util.HeaderUtil;

public final class InvocationContextHolder {
  public static final String SERIALIZE_KEY = "x-invocation-context";

  public static final String ATTRIBUTE_KEY = "x-invocation-context";

  private static final ThreadLocal<InvocationContext> INVOCATION_CONTEXT = new ThreadLocal<>();

  public static InvocationContext getOrCreateInvocationContext() {
    InvocationContext result = INVOCATION_CONTEXT.get();
    if (result == null) {
      result = new InvocationContext();
      INVOCATION_CONTEXT.set(result);
    }
    return result;
  }

  public static InvocationContext deserializeAndCreate(String context) {
    InvocationContext result = deserialize(context);
    INVOCATION_CONTEXT.set(result);
    return result;
  }

  public static InvocationContext deserialize(String context) {
    InvocationContext result = new InvocationContext();
    result.putContext(HeaderUtil.deserialize(context));
    return result;
  }

  public static String serialize(InvocationContext context) {
    if (context == null) {
      return "";
    }
    return HeaderUtil.serialize(context.getContext());
  }

  public static void clearInvocationContext() {
    INVOCATION_CONTEXT.set(null);
  }

  public static void setInvocationContext(InvocationContext context) {
    INVOCATION_CONTEXT.set(context);
  }
}
