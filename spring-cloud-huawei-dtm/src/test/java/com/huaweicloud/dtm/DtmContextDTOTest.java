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

package com.huaweicloud.dtm;

import static org.junit.Assert.assertEquals;

import com.huaweicloud.dtm.DtmContextDTO;
import org.junit.Test;

import com.huawei.middleware.dtm.client.context.DTMContext;

/**
 * @Author wangqijun
 * @Date 10:34 2019-09-29
 **/
public class DtmContextDTOTest {

  @Test
  public void fromDtmContext() {
    DTMContext dtmContext = DTMContext.getDTMContext();
    dtmContext.setGlobalTxId(100);
    dtmContext.setBranchTxId(200);
    dtmContext.setParentTxId(300);
    dtmContext.setChannelKey("ck");
    dtmContext.setCustomizedData("cz");
    DtmContextDTO dto = DtmContextDTO.fromDtmContext(dtmContext);
    assertEquals(dto.getGlobalTxId(), dtmContext.getGlobalTxId());
    assertEquals(dto.getBranchTxId(), dtmContext.getBranchTxId());
    assertEquals(dto.getChannelKey(), dtmContext.getChannelKey());
    assertEquals(dto.getCustomizedData(), dtmContext.getCustomizedData());
    assertEquals(dto.getGlobalOptionalData(), dtmContext.getGlobalOptionalData());
    assertEquals(dto.getBranchOptionalData(), dtmContext.getBranchOptionalData());
  }
}