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
package com.huaweicloud.hessian;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestHessianReadWrite {
  @Test
  public void testTypeClassArrayNotNull() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Hessian2ObjectOutput output = new Hessian2ObjectOutput(os);
    TypeClassArray param = new TypeClassArray();
    TypeInterfaceImpl impl = new TypeInterfaceImpl("hello");
    TypeInterface[] array = new TypeInterface[] {impl};
    param.setClassArray(array);
    output.writeObject(param);
    output.flushBuffer();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    Hessian2ObjectInput input = new Hessian2ObjectInput(is);
    TypeClassArray result = input.readObject(TypeClassArray.class);
    Assertions.assertEquals("hello", result.getClassArray()[0].name());
  }

  @Test
  public void testTypeClassArrayIsNull() throws Exception {
    ByteArrayOutputStream os = new ByteArrayOutputStream();
    Hessian2ObjectOutput output = new Hessian2ObjectOutput(os);
    TypeClassArray param = new TypeClassArray();
    output.writeObject(param);
    output.flushBuffer();

    ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
    Hessian2ObjectInput input = new Hessian2ObjectInput(is);
    TypeClassArray result = input.readObject(TypeClassArray.class);
    Assertions.assertEquals(null, result.getClassArray());
  }
}
