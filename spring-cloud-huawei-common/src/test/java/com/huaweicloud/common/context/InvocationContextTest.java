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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvocationContextTest {
  @Test
  public void test_context_lifecycle() {
    String context = "{\"foo\":\"foo\"}";

    InvocationContext invocationContext = InvocationContextHolder.create(context);
    Assertions.assertEquals("foo", invocationContext.getContext("foo"));

    invocationContext = InvocationContextHolder.getInvocationContext();
    Assertions.assertEquals("foo", invocationContext.getContext("foo"));

    invocationContext.putLocalContext("bar", "bar");
    invocationContext.putContext("foo2", "foo2");
    Assertions.assertEquals("bar", invocationContext.getLocalContext("bar"));

    String serialized = InvocationContextHolder.serialize(invocationContext);
    invocationContext = InvocationContextHolder.create(serialized);
    Assertions.assertEquals("foo", invocationContext.getContext("foo"));
    Assertions.assertEquals("foo2", invocationContext.getContext("foo2"));
    Assertions.assertNull(invocationContext.getLocalContext("bar"));
  }

  @Test
  public void test_context_lifecycleFromEmpty() {
    String context = "";

    InvocationContext invocationContext = InvocationContextHolder.create(context);
    Assertions.assertNull(invocationContext.getLocalContext("foo"));

    invocationContext = InvocationContextHolder.getInvocationContext();
    Assertions.assertNull(invocationContext.getLocalContext("foo"));

    invocationContext.putLocalContext("bar", "bar");
    Assertions.assertEquals("bar", invocationContext.getLocalContext("bar"));

    String serialized = InvocationContextHolder.serialize(invocationContext);
    invocationContext = InvocationContextHolder.create(serialized);
    Assertions.assertNull(invocationContext.getLocalContext("foo"));
    Assertions.assertNull(invocationContext.getLocalContext("bar"));
  }
}
