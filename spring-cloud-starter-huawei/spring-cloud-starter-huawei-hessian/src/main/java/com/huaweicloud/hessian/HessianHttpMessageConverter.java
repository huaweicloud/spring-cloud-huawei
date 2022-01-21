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

package com.huaweicloud.hessian;

import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectInput;
import org.apache.dubbo.common.serialize.hessian2.Hessian2ObjectOutput;
import org.springframework.http.HttpInputMessage;
import org.springframework.http.HttpOutputMessage;
import org.springframework.http.MediaType;
import org.springframework.http.converter.AbstractGenericHttpMessageConverter;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;

import java.io.IOException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

public class HessianHttpMessageConverter extends AbstractGenericHttpMessageConverter<Object> {
  public static final String HESSIAN_MEDIA_TYPE_VALUE = "x-application/hessian2";

  public static final MediaType HESSIAN_MEDIA_TYPE = MediaType.valueOf(HESSIAN_MEDIA_TYPE_VALUE);

  public HessianHttpMessageConverter() {
    super(HESSIAN_MEDIA_TYPE);
  }

  @Override
  protected boolean supports(Class<?> clazz) {
    return true;
  }

  @Override
  protected boolean canWrite(MediaType mediaType) {
    return HESSIAN_MEDIA_TYPE.equalsTypeAndSubtype(mediaType);
  }

  @Override
  protected boolean canRead(MediaType mediaType) {
    return HESSIAN_MEDIA_TYPE.equalsTypeAndSubtype(mediaType);
  }

  @Override
  protected Object readInternal(Class<?> clazz, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    Hessian2ObjectInput input = new Hessian2ObjectInput(inputMessage.getBody());
    try {
      return input.readObject(clazz);
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }

  @Override
  protected void writeInternal(Object o, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    Hessian2ObjectOutput output = new Hessian2ObjectOutput(outputMessage.getBody());
    output.writeObject(o);
    output.flushBuffer();
  }

  @Override
  protected void writeInternal(Object o, Type type, HttpOutputMessage outputMessage)
      throws IOException, HttpMessageNotWritableException {
    Hessian2ObjectOutput output = new Hessian2ObjectOutput(outputMessage.getBody());
    output.writeObject(o);
    output.flushBuffer();
  }

  @Override
  public Object read(Type type, Class<?> contextClass, HttpInputMessage inputMessage)
      throws IOException, HttpMessageNotReadableException {
    Hessian2ObjectInput input = new Hessian2ObjectInput(inputMessage.getBody());
    try {
      while (type instanceof ParameterizedType) {
        type = ((ParameterizedType) type).getRawType();
      }

      if (type instanceof Class<?>) {
        return input.readObject((Class<?>) type);
      }

      throw new IOException("not supported type " + type.getTypeName());
    } catch (ClassNotFoundException e) {
      throw new IOException(e);
    }
  }
}
