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
 * @Date 22:13 2019-07-30
 **/
public class NetUtilTest {


  @Test
  public void getLocalHost() {
    Assert.assertNotNull(NetUtil.getLocalHost());
  }

  @Test
  public void getPort() {
    Integer port = NetUtil.getPort("http://127.0.0.1:30103/#!/sc/f1532d0479261777281fe3d94b15c463f8b6fcf7/instance");
    Assert.assertEquals(port, new Integer(30103));
  }

  @Test
  public void getHost() {
    String host = NetUtil.getHost("http://127.0.0.1:30103/#!/sc/f1532d0479261777281fe3d94b15c463f8b6fcf7/instance");
    Assert.assertEquals(host, "127.0.0.1");
  }
}