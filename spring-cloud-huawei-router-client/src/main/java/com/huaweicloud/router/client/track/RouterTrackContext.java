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
package com.huaweicloud.router.client.track;

import java.util.Map;

/**
 * 当前使用线程上下文获取服务端请求的header， 不适用于Reactive模式。
 **/
public class RouterTrackContext {
  public static final String ROUTER_TRACK_HEADER = "X-RouterContext";

  private static ThreadLocal<String> requestHeaderThreadLocal = new ThreadLocal<>();

  public static void remove() {
    requestHeaderThreadLocal.remove();
  }

  public static String getRequestHeader() {
    return requestHeaderThreadLocal.get();
  }

  public static void setRequestHeader(String requestHeader) {
    requestHeaderThreadLocal.set(requestHeader);
  }
}
