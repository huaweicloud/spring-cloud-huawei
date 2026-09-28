/*

  * Copyright (C) 2020-2026 Huawei Technologies Co., Ltd. All rights reserved.

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

package com.huaweicloud.common.util;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * @Author wangqijun
 * @Date 17:41 2019-08-07
 **/
public class URLUtilTest {

  @Test
  public void transform() {
    Assertions.assertEquals(URLUtil.transform("rest://aa.com"), "http://aa.com");
    Assertions.assertEquals(URLUtil.transform("rest://aa.com/?sslEnabled=true"), "https://aa.com/");
    Assertions.assertNull(URLUtil.transform(null));
  }

  @Test
  public void splitIpPort() {
    String[] res = URLUtil.splitIpPort("http://0.0.0.0:3000/?end=udu");
    Assertions.assertEquals(res[0], "0.0.0.0");
    Assertions.assertEquals(res[1], "3000");
  }

  @Test
  public void splitIpPortWithoutPath() {
    Assertions.assertArrayEquals(
        new String[] {"127.0.0.1", "8080"},
        URLUtil.splitIpPort("rest://127.0.0.1:8080"));
    Assertions.assertArrayEquals(
        new String[] {"127.0.0.1", "8080"},
        URLUtil.splitIpPort("rest://127.0.0.1:8080?sslEnabled=true"));
  }

  @Test
  public void isEquals() {
    String url1 = "http://127.0.0.1:3030";
    String url2 = "http://127.0.0.1:3030";
    Assertions.assertTrue(URLUtil.isEquals(url1, url2));
    String url3 = "http://www.ddd.com";
    String url4 = "http://www.ddd.com";
    Assertions.assertTrue(URLUtil.isEquals(url3, url4));
    String url5 = "http://127.0.0.1:3030";
    String url6 = "http://127.0.0.1:3030/";
    Assertions.assertTrue(URLUtil.isEquals(url5, url6));
    Assertions.assertFalse(URLUtil.isEquals(null, url6));
    Assertions.assertFalse(URLUtil.isEquals(null, null));
  }

  @Test
  public void transformWithMultipleParameters() {
    Assertions.assertEquals(
        "https://example.com:8080/path",
        URLUtil.transform("rest://example.com:8080/path?region=cn&sslEnabled=true"));
    Assertions.assertEquals(
        "http://example.com/path",
        URLUtil.transform("rest://example.com/path?sslEnabled=false"));
  }

  @Test
  public void dealMultiUrl() {
    Assertions.assertEquals(List.of(), URLUtil.dealMultiUrl(null));
    Assertions.assertEquals(List.of(), URLUtil.dealMultiUrl(""));
    Assertions.assertEquals(List.of("rest://one:8080"), URLUtil.dealMultiUrl("rest://one:8080"));
    Assertions.assertEquals(
        List.of("rest://one:8080", "rest://two:8081"),
        URLUtil.dealMultiUrl("rest://one:8080,,rest://two:8081,"));
  }
}
