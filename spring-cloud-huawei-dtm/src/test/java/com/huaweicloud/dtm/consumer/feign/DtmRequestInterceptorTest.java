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

package com.huaweicloud.dtm.consumer.feign;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import com.huaweicloud.dtm.util.DtmConstants;

import com.huawei.middleware.dtm.client.context.DTMContext;

import feign.RequestTemplate;

/**
 * @Author wangqijun
 * @Date 10:52 2019-09-29
 **/
public class DtmRequestInterceptorTest {

  @Test
  public void apply() {
    DtmRequestInterceptor dtmRequestInterceptor = new DtmRequestInterceptor();
    RequestTemplate template = new RequestTemplate();
    DTMContext dtmContext = DTMContext.getDTMContext();
    dtmContext.setGlobalTxId(100);
    dtmRequestInterceptor.apply(template);
    assertNotNull(template.header(DtmConstants.DTM_CONTEXT));
  }
}