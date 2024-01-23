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
 * @Date 22:13 2019-07-30
 **/
public class NetUtilTest {


  @Test
  public void getLocalHost() {
    Assertions.assertNotNull(NetUtil.getLocalHost());
  }

  @Test
  public void getPort() {
    Integer port = NetUtil.getPort("http://127.0.0.1:30103/#!/sc/f1532d0479261777281fe3d94b15c463f8b6fcf7/instance");
    Assertions.assertEquals(port, new Integer(30103));
  }

  @Test
  public void getHost() {
    String host = NetUtil.getHost("http://127.0.0.1:30103/#!/sc/f1532d0479261777281fe3d94b15c463f8b6fcf7/instance");
    Assertions.assertEquals(host, "127.0.0.1");
  }
}