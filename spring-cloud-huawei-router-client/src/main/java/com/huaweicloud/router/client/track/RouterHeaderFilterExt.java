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
 * @Author GuoYl123
 * @Date 2019/12/12
 **/
public interface RouterHeaderFilterExt extends Comparable<RouterHeaderFilterExt> {

  default int getOrder() {
    return 0;
  }

  default boolean enabled() {
    return true;
  }

  Map<String, String> doFilter(Map<String, String> invokeHeader);

  @Override
  default int compareTo(RouterHeaderFilterExt o) {
    return Integer.compare(this.getOrder(), o.getOrder());
  }
}
