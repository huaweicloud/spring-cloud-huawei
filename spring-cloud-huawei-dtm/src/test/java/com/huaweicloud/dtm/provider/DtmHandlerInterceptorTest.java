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

import static org.junit.Assert.assertEquals;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.junit.Test;

import com.huawei.middleware.dtm.client.context.DTMContext;
import com.huaweicloud.dtm.DtmConstants;

import io.vertx.core.json.Json;
import mockit.Expectations;
import mockit.Injectable;

/**
 * @Author wangqijun
 * @Date 10:45 2019-09-29
 **/
public class DtmHandlerInterceptorTest {

  @Test
  public void preHandle(@Injectable HttpServletRequest request) throws Exception {
    DtmHandlerInterceptor dtmHandlerInterceptor = new DtmHandlerInterceptor();
    Map<String, String> header = new HashMap<>();
    String expectTxId = "100";
    header.put(DTMContext.GLOBAL_TX_ID_KEY, expectTxId);
    new Expectations() {
      {
        request.getHeader(DtmConstants.DTM_CONTEXT);
        result = Json.encode(header);
      }
    };
    dtmHandlerInterceptor.preHandle(request, null, null);

    assertEquals(expectTxId, DTMContext.GLOBAL_TX_ID);
  }
}