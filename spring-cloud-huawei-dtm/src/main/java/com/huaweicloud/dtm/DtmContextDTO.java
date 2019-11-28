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

import java.util.HashMap;
import java.util.Map;

import com.huawei.middleware.dtm.client.context.DTMContext;

/**
 * @Author wangqijun
 * @Date 09:54 2019-09-27
 **/
public class DtmContextDTO {
  private String customizedData = "";

  private long globalTxId = -1L;

  private long branchTxId = -1L;

  private String channelKey = "";

  private Map<String, String> globalOptionalData = new HashMap();

  private Map<String, String> branchOptionalData = new HashMap();

  public DtmContextDTO() {
  }

  public static DtmContextDTO fromDtmContext(DTMContext dtmContext) {
    DtmContextDTO dtmContextDTO = new DtmContextDTO();
    dtmContextDTO.setGlobalTxId(dtmContext.getGlobalTxId());
    dtmContextDTO.setBranchTxId(dtmContext.getBranchTxId());
    dtmContextDTO.setChannelKey(dtmContext.getChannelKey());
    dtmContextDTO.setGlobalOptionalData(dtmContext.getGlobalOptionalData());
    dtmContextDTO.setBranchOptionalData(dtmContext.getBranchOptionalData());
    dtmContextDTO.setCustomizedData(dtmContext.getCustomizedData());
    return dtmContextDTO;
  }

  public String getCustomizedData() {
    return this.customizedData;
  }

  public void setCustomizedData(String customizedData) {
    this.customizedData = customizedData;
  }

  public long getGlobalTxId() {
    return this.globalTxId;
  }

  public void setGlobalTxId(long globalTxId) {
    this.globalTxId = globalTxId;
  }

  public long getBranchTxId() {
    return this.branchTxId;
  }

  public void setBranchTxId(long branchTxId) {
    this.branchTxId = branchTxId;
  }

  public String getChannelKey() {
    return this.channelKey;
  }

  public void setChannelKey(String channelKey) {
    this.channelKey = channelKey;
  }

  public Map<String, String> getGlobalOptionalData() {
    return this.globalOptionalData;
  }

  public void setGlobalOptionalData(Map<String, String> globalOptionalData) {
    this.globalOptionalData = globalOptionalData;
  }

  public Map<String, String> getBranchOptionalData() {
    return this.branchOptionalData;
  }

  public void setBranchOptionalData(Map<String, String> branchOptionalData) {
    this.branchOptionalData = branchOptionalData;
  }
}
