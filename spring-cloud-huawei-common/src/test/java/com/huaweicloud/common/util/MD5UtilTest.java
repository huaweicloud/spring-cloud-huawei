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

import org.junit.Assert;
import org.junit.Test;

/**
 * @Author wangqijun
 * @Date 15:38 2019-11-06
 **/
public class MD5UtilTest {

  @Test
  public void encrypt() {
    String computeValue = "dasfjkl;sdjfkldsafjodsiu29-w0483290-48230-8idfsopafjdls;afkd;safkd;safdsaf";
    Assert.assertEquals(MD5Util.encrypt(computeValue), MD5Util.encrypt(computeValue));
    String changeValue = "dasfjkl;sdjfkldsafjodsiu29-w0483290-48230-8idfsopafjdls;afkd;safkd;safdsa5";
    Assert.assertNotEquals(MD5Util.encrypt(computeValue), MD5Util.encrypt(changeValue));
  }
}