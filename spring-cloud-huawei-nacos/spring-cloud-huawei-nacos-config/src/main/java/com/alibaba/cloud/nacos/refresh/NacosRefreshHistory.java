/*
 * Copyright 2013-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.cloud.nacos.refresh;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * Forked from com.alibaba.cloud.nacos.refresh.NacosRefreshHistory.java
 */
public class NacosRefreshHistory {
  private static final int MAX_SIZE = 20;

  private final List<Record> records = new LinkedList<>();

  private final ThreadLocal<DateFormat> DATE_FORMAT = ThreadLocal
      .withInitial(() -> new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"));

  /**
   * recommend to use
   * {@link NacosRefreshHistory#addRefreshRecord(String, String, String)}.
   * @param dataId dataId
   * @param md5 md5
   */
  @Deprecated
  public void add(String dataId, String md5) {
    ((LinkedList<Record>) records).addFirst(
        new Record(DATE_FORMAT.get().format(new Date()), dataId, "", md5));
    if (records.size() > MAX_SIZE) {
      ((LinkedList<Record>) records).removeLast();
    }
  }

  public void addRefreshRecord(String dataId, String group, String data) {
    ((LinkedList<Record>) records).addFirst(new Record(DATE_FORMAT.get().format(new Date()), dataId, group,
        md5(data)));
    if (records.size() > MAX_SIZE) {
      ((LinkedList<Record>) records).removeLast();
    }
  }

  public List<Record> getRecords() {
    return records;
  }

  private String md5(String data) {
    if (StringUtils.isEmpty(data)) {
      return null;
    }
    MessageDigest md;
    try {
      md = MessageDigest.getInstance("MD5");
    } catch (NoSuchAlgorithmException ignored) {
      return "unable to get md5";
    }
    return new BigInteger(1, md.digest(data.getBytes(StandardCharsets.UTF_8)))
        .toString(16);
  }

  static class Record {

    private final String timestamp;

    private final String dataId;

    private final String group;

    private final String md5;

    Record(String timestamp, String dataId, String group, String md5) {
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
