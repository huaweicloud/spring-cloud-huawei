/*

 * Copyright (C) 2020-2024 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.nacos.config.refresh;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;

import org.apache.commons.lang3.StringUtils;

public class NacosConfigRefreshCache {
  private static final int MAX_SIZE = 20;

  private final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

  public void addRefreshRecord(String dataId, String group, String configInfo) {
    LinkedList<Record> records = new LinkedList<>();
    records.addFirst(new Record(dateFormat.format(new Date()), dataId, group, md5Data(configInfo)));
    if (records.size() > MAX_SIZE) {
      records.removeLast();
    }
  }

  private String md5Data(String configInfo) {
    if (StringUtils.isEmpty(configInfo)) {
      return null;
    }
    MessageDigest md5;
    try {
      md5 = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException e) {
      return "unable to get md5";
    }
    return new BigInteger(1, md5.digest(configInfo.getBytes(StandardCharsets.UTF_8))).toString(16);
  }

  static class Record {
    private final String timestamp;

    private final String dataId;

    private final String group;

    private final String md5;

    public Record(String timestamp, String dataId, String group, String md5) {
      this.timestamp = timestamp;
      this.dataId = dataId;
      this.group = group;
      this.md5 = md5;
    }

    public String getTimestamp() {
      return timestamp;
    }

    public String getDataId() {
      return dataId;
    }

    public String getGroup() {
      return group;
    }

    public String getMd5() {
      return md5;
    }
  }
}
