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

package com.huaweicloud.common.util;

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
}
