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

import org.apache.commons.codec.net.URLCodec;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InvocationContextTest {
  URLCodec coded = new URLCodec("UTF-8");

  @Test
  public void test_context_lifecycle() throws Exception {
    String context = coded.encode("{\"foo\":\"foo\"}");

    InvocationContext invocationContext = InvocationContextHolder.deserializeAndCreate(context);
    Assertions.assertEquals("foo", invocationContext.getContext("foo"));

    invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    Assertions.assertEquals("foo", invocationContext.getContext("foo"));

    invocationContext.putLocalContext("bar", "bar");
    invocationContext.putContext("foo2", "foo2");
    Assertions.assertEquals("bar", invocationContext.getLocalContext("bar"));

    String serialized = InvocationContextHolder.serialize(invocationContext);
    invocationContext = InvocationContextHolder.deserializeAndCreate(serialized);
    Assertions.assertEquals("foo", invocationContext.getContext("foo"));
    Assertions.assertEquals("foo2", invocationContext.getContext("foo2"));
    Assertions.assertNull(invocationContext.getLocalContext("bar"));
  }

  @Test
  public void test_context_lifecycleFromEmpty() {
    String context = "";

    InvocationContext invocationContext = InvocationContextHolder.deserializeAndCreate(context);
    Assertions.assertNull(invocationContext.getLocalContext("foo"));

    invocationContext = InvocationContextHolder.getOrCreateInvocationContext();
    Assertions.assertNull(invocationContext.getLocalContext("foo"));

    invocationContext.putLocalContext("bar", "bar");
    Assertions.assertEquals("bar", invocationContext.getLocalContext("bar"));

    String serialized = InvocationContextHolder.serialize(invocationContext);
    invocationContext = InvocationContextHolder.deserializeAndCreate(serialized);
    Assertions.assertNull(invocationContext.getLocalContext("foo"));
    Assertions.assertNull(invocationContext.getLocalContext("bar"));
  }

  @Test
  public void test_empty_context() {
    InvocationContext invocationContext = InvocationContextHolder.deserializeAndCreate(null);
    Assertions.assertNull(invocationContext.getContext("foo"));
    invocationContext = InvocationContextHolder.deserializeAndCreate("");
    Assertions.assertNull(invocationContext.getContext("foo"));

    String context = InvocationContextHolder.serialize(null);
    Assertions.assertEquals("", context);
  }
}
