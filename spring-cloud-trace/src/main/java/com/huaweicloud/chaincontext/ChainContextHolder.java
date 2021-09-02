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
package com.huaweicloud.chaincontext;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class ChainContextHolder extends ConcurrentHashMap<String, Object> {

  private static final long serialVersionUID = -5792996927212791314L;

  protected static Class<? extends ChainContextHolder> contextClass = ChainContextHolder.class;

  protected static final ThreadLocal<? extends ChainContextHolder> THREAD_LOCAL = new InheritableThreadLocal<ChainContextHolder>() {
    @Override
    protected ChainContextHolder initialValue() {
      try {
        return contextClass.newInstance();
      } catch (Throwable e) {
        throw new RuntimeException(e);
      }
    }
  };


  public static void setContextClass(Class<? extends ChainContextHolder> clazz) {
    contextClass = clazz;
  }


  public static final ChainContextHolder getCurrentContext() {
    return THREAD_LOCAL.get();
  }


  public void unset() {
    this.clear();
    THREAD_LOCAL.remove();
  }


  public Object getDefault(String key, Object defaultValue) {
    return Optional.ofNullable(get(key)).orElse(defaultValue);
  }
}
