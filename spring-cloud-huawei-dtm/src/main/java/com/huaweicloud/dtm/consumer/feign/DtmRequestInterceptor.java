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
package com.huaweicloud.dtm.consumer.feign;

import com.huaweicloud.dtm.DtmContextDTO;
import com.huaweicloud.dtm.util.DtmConstants;

import com.huawei.middleware.dtm.client.context.DTMContext;

import io.vertx.core.json.Json;
import feign.RequestInterceptor;
import feign.RequestTemplate;

/**
 * @Author wangqijun
 * @Date 09:52 2019-09-26
 **/
public class DtmRequestInterceptor implements RequestInterceptor {
  @Override
  public void apply(RequestTemplate template) {
    DTMContext dtmContext = DTMContext.getDTMContext();
    long gid = dtmContext.getGlobalTxId();
    if (gid != -1) {
      DtmContextDTO dtmContextDTO = DtmContextDTO.fromDtmContext(dtmContext);
      template.header(DtmConstants.DTM_CONTEXT, Json.encode(dtmContextDTO));
    }
  }
}
