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

import java.io.IOException;
import java.io.OutputStream;

import org.apache.dubbo.common.serialize.Cleanable;
import org.apache.dubbo.common.serialize.ObjectOutput;
import org.apache.dubbo.common.serialize.hessian2.Hessian2SerializerFactory;

import com.alibaba.com.caucho.hessian.io.Hessian2Output;

/**
 * Hessian2 object output implementation. This file is based on original Dubbo implementation.
 * This purpose is to support non-serializable model.
 */
public class Hessian2ObjectOutput implements ObjectOutput, Cleanable {

  private static ThreadLocal<Hessian2Output> OUTPUT_TL = ThreadLocal.withInitial(() -> {
    Hessian2Output h2o = new Hessian2Output(null);
    Hessian2SerializerFactory factory = new Hessian2SerializerFactory();
    factory.setAllowNonSerializable(true);
    h2o.setSerializerFactory(factory);
    h2o.setCloseStreamOnClose(true);
    return h2o;
  });

  private final Hessian2Output mH2o;

  public Hessian2ObjectOutput(OutputStream os) {
    mH2o = OUTPUT_TL.get();
    mH2o.init(os);
  }

  @Override
  public void writeBool(boolean v) throws IOException {
    mH2o.writeBoolean(v);
  }

  @Override
  public void writeByte(byte v) throws IOException {
    mH2o.writeInt(v);
  }

  @Override
  public void writeShort(short v) throws IOException {
    mH2o.writeInt(v);
  }

  @Override
  public void writeInt(int v) throws IOException {
    mH2o.writeInt(v);
  }

  @Override
  public void writeLong(long v) throws IOException {
    mH2o.writeLong(v);
  }

  @Override
  public void writeFloat(float v) throws IOException {
    mH2o.writeDouble(v);
  }

  @Override
  public void writeDouble(double v) throws IOException {
    mH2o.writeDouble(v);
  }

  @Override
  public void writeBytes(byte[] b) throws IOException {
    mH2o.writeBytes(b);
  }

  @Override
  public void writeBytes(byte[] b, int off, int len) throws IOException {
    mH2o.writeBytes(b, off, len);
  }

  @Override
  public void writeUTF(String v) throws IOException {
    mH2o.writeString(v);
  }

  @Override
  public void writeObject(Object obj) throws IOException {
    mH2o.writeObject(obj);
  }

  @Override
  public void flushBuffer() throws IOException {
    mH2o.flushBuffer();
  }

  public OutputStream getOutputStream() throws IOException {
    return mH2o.getBytesOutputStream();
  }

  @Override
  public void cleanup() {
    if(mH2o != null) {
      mH2o.reset();
    }
  }
}
