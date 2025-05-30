/*

 * Copyright (C) 2020-2025 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.rocketmq.grayscale.holder;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public final class RequestGrayHeaderHolder {
  private static final ThreadLocal<Map<String, String>> INVOCATION_GRAY_HEADERS = new ThreadLocal<>();

  public static void setRequestGrayHeader(Map<String, String> headers) {
    INVOCATION_GRAY_HEADERS.set(headers);
  }

  public static Map<String, String> getRequestGrayHeader() {
    return INVOCATION_GRAY_HEADERS.get();
  }
}
