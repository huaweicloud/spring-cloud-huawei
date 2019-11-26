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

package com.huaweicloud.common.util;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Author wangqijun
 * @Date 15:05 2019-11-06
 **/
public class MD5Util {
  private static final Logger LOGGER = LoggerFactory.getLogger(NetUtil.class);

  public static String encrypt(String dataStr) {
    MessageDigest messageDigest = null;
    String result = "";
    try {
      messageDigest = MessageDigest.getInstance("MD5");
      messageDigest.update(dataStr.getBytes(StandardCharsets.UTF_8));
      result = new BigInteger(1, messageDigest.digest(dataStr.getBytes(StandardCharsets.UTF_8)))
          .toString(16);
    } catch (NoSuchAlgorithmException e) {
      LOGGER.error("Failed to generate MD5 . ", e);
    }
    return result;
  }
}
