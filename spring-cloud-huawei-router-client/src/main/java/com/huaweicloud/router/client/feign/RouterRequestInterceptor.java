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

package com.huaweicloud.router.client.feign;

import com.huaweicloud.router.client.header.HeaderPassUtil;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import java.util.HashMap;
import java.util.Map;
import org.springframework.util.CollectionUtils;

/**
 * @Author GuoYl123
 * @Date 2019/12/12
 **/
public class RouterRequestInterceptor implements RequestInterceptor {

  private static final String ROUTER_HEADER = "X-RouterContext";

  /**
   * header pass
   *
   * @param requestTemplate
   */
  @Override
  public void apply(RequestTemplate requestTemplate) {
    Map<String, String> allHeaders = new HashMap<>();
    requestTemplate.headers().forEach((k, v) -> {
      if (CollectionUtils.isEmpty(v)) {
        allHeaders.put(k, v.toArray()[0].toString());
      }
    });
    requestTemplate.header(ROUTER_HEADER, HeaderPassUtil.getPassHeaderString(allHeaders));
  }
}
