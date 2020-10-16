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
package com.huaweicloud.dtm.provider;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.servicecomb.foundation.common.utils.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import com.fasterxml.jackson.core.type.TypeReference;
import com.huaweicloud.dtm.DtmConstants;

/**
 * Implement filter, convert the data before the request is processed, before being processed by the business
 * @Author wangqijun
 * @Date 17:21 2019-09-25
 **/
public class DtmHandlerInterceptor implements HandlerInterceptor {
  private static final Logger LOGGER = LoggerFactory.getLogger(DtmHandlerInterceptor.class);

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
    if (DtmConstants.DTM_CONTEXT_IM_METHOD == null) {
      return true;
    }
    //get header then fill to DTMContext for business use it
    try {
      String dtmHeader = request.getHeader(DtmConstants.DTM_CONTEXT);
      if (StringUtils.isNotEmpty(dtmHeader)) {
        Map<String, String> context = JsonUtils.OBJ_MAPPER
            .readValue(dtmHeader, new TypeReference<Map<String, String>>() {
            });
        DtmConstants.DTM_CONTEXT_IM_METHOD.invoke(null, context);
      }
    } catch (Throwable e) {
      LOGGER.warn("Failed to import dtm context", e);
    }
    return true;
  }
}
